package haennihaesseo.sandoll.global.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@Slf4j
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

  private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

  public CustomOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
    this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository, "/oauth2/authorization");
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
    return customizeState(request, authorizationRequest);
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
    OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
    return customizeState(request, authorizationRequest);
  }

  private OAuth2AuthorizationRequest customizeState(HttpServletRequest request, OAuth2AuthorizationRequest authorizationRequest) {
    if (authorizationRequest == null) {
      return null;
    }

    String redirect = request.getParameter("redirect");
    log.info("CustomOAuth2AuthorizationRequestResolver - redirect parameter: {}", redirect);
    if (redirect == null || redirect.isBlank()) {
      return authorizationRequest;
    }

    if (!isValidRedirectPath(redirect)) {
      return authorizationRequest;
    }

    String originalState = authorizationRequest.getState();
    String encodedRedirect = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(redirect.getBytes(StandardCharsets.UTF_8));
    String customState = originalState + "_" + encodedRedirect;

    return OAuth2AuthorizationRequest.from(authorizationRequest)
        .state(customState)
        .build();
  }

  private boolean isValidRedirectPath(String redirect) {
    // 추후 더 강력하게 검증 로직 추가 가능 - ex. prefix 정행두기 (/app, /user 등)
    return redirect.startsWith("/") && !redirect.startsWith("//");
  }
}
