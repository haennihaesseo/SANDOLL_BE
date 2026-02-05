package haennihaesseo.sandoll.global.auth.util;

import haennihaesseo.sandoll.domain.user.entity.User;
import haennihaesseo.sandoll.domain.user.repository.UserRepository;
import haennihaesseo.sandoll.global.auth.principal.UserPrincipal;
import haennihaesseo.sandoll.global.exception.GlobalException;
import haennihaesseo.sandoll.global.status.ErrorStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String token = extractToken(request);

    // 1) 토큰 없으면 그냥 통과
    if (token == null || token.isBlank()) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      jwtUtil.validateAccessToken(token);
      Long userId = jwtUtil.getUserIdFromAccessToken(token);
      User user = userRepository.findByUserId(userId)
          .orElseThrow(() -> new GlobalException(ErrorStatus.NOT_FOUND));

      UserPrincipal userPrincipal = new UserPrincipal(user);
      Authentication authentication = new UsernamePasswordAuthenticationToken(
          userPrincipal, null, userPrincipal.getAuthorities());

      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (GlobalException e) {
      log.error("토큰 검증 실패: {}", e.getErrorStatus());
      handleTokenException(response, e);
      return;
    } catch (Exception e) {
      log.error("토큰 검증 중 알 수 없는 오류 발생: {}", e.getMessage());
      handleException(response, e);
      return;
    }
    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    // 특정 경로는 필터링하지 않도록 설정
    String path = request.getRequestURI();
    return path.contains("/oauth2/")
        || path.startsWith("/api/token")
        || path.startsWith("/actuator/health")
        || path.contains("/login/")
        || path.equals("/")
        || path.startsWith("/swagger-ui")
        || path.startsWith("/v3/api-docs") || path.startsWith("/api/letter/view");
  }

  private String extractToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  private void handleTokenException(HttpServletResponse response, GlobalException e) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    String jsonResponse = String.format("{\"isSuccess\": \"false\", \"code\": \"%s\", \"message\": \"%s\"}", e.getErrorStatus().getCode(), e.getMessage());
    response.getWriter().write(jsonResponse);
  }

  private void handleException(HttpServletResponse response, Exception e) throws IOException {
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    String jsonResponse = String.format("{\"isSuccess\": \"false\", \"code\": \"500\", \"message\": \"%s\"}", e.getMessage());
    response.getWriter().write(jsonResponse);
  }

}