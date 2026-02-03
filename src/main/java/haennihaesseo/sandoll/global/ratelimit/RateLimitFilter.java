package haennihaesseo.sandoll.global.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import haennihaesseo.sandoll.global.response.ApiResponse;
import haennihaesseo.sandoll.global.status.ErrorStatus;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final ProxyManager<String> proxyManager;
    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper;

    private static final List<String> RATE_LIMITED_PATHS = List.of(
            "/api/letter/voice"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Rate limiting이 비활성화되어 있으면 바로 통과
        if (!rateLimitConfig.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestPath = request.getRequestURI();

        // Rate limit 대상 경로가 아니면 통과
        if (!isRateLimitedPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }
        String clientIp = getClientIp(request);

        // 화이트리스트 IP는 통과
        if (isWhitelisted(clientIp)) {
            log.debug("[RateLimit] 화이트리스트 IP 통과: {}", clientIp);
            filterChain.doFilter(request, response);
            return;
        }

        // 버킷에서 토큰 소비 시도
        Bucket bucket = resolveBucket(clientIp);
        if (bucket.tryConsume(1)) {
            log.debug("[RateLimit] 요청 허용 - IP: {}, 남은 토큰: {}", clientIp, bucket.getAvailableTokens());
            filterChain.doFilter(request, response);
        } else {
            log.warn("[RateLimit] 요청 거부 - IP: {}, 경로: {}", clientIp, requestPath);
            sendRateLimitResponse(response);
        }
    }

    private boolean isRateLimitedPath(String path) {
        return RATE_LIMITED_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isWhitelisted(String clientIp) {
        return rateLimitConfig.getWhitelistIpList().contains(clientIp);
    }

    private Bucket resolveBucket(String clientIp) {
        String key = "rate-limit:" + clientIp;
        Supplier<BucketConfiguration> configSupplier = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(rateLimitConfig.getRequestsPerHour())
                        .refillGreedy(rateLimitConfig.getRequestsPerHour(), Duration.ofHours(1))
                        .build())
                .build();

        return proxyManager.builder().build(key, configSupplier);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For의 첫 번째 IP가 실제 클라이언트 IP
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
        ErrorStatus errorStatus = ErrorStatus.TOO_MANY_REQUESTS;
        response.setStatus(errorStatus.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ResponseEntity<ApiResponse<Void>> errorResponse = ApiResponse.fail(errorStatus);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse.getBody()));
    }
}