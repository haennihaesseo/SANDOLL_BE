package haennihaesseo.sandoll.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import haennihaesseo.sandoll.global.auth.handler.CustomOAuth2AuthorizationRequestResolver;
import haennihaesseo.sandoll.global.auth.handler.OAuthLoginFailureHandler;
import haennihaesseo.sandoll.global.auth.handler.OAuthLoginSuccessHandler;
import haennihaesseo.sandoll.global.auth.util.JwtFilter;
import haennihaesseo.sandoll.global.ratelimit.RateLimitFilter;
import haennihaesseo.sandoll.global.response.ApiResponse;
import haennihaesseo.sandoll.global.status.ErrorStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
  private final JwtFilter jwtFilter;
  private final RateLimitFilter rateLimitFilter;
  private final OAuthLoginSuccessHandler oAuthLoginSuccessHandler;
  private final OAuthLoginFailureHandler oAuthLoginFailureHandler;
  private final ObjectMapper objectMapper;
  private final ClientRegistrationRepository clientRegistrationRepository;

  @Value("${app.server-url}")
  private String serverUrl;

  private static final String[] PUBLIC_URLS = {
      "/",
      "/oauth2/authorization/kakao",
      "/api/token/**",
      "/actuator/health",
      "/swagger-ui/**",
      "/swagger-ui.html",
      "/v3/api-docs/**",
      "/api/letter/voice",
      "/api/letter",
      "/api/letter/view",
      "/api/font/upload",
      "/api/deco/**",
      "/api/letter/font",
      "/api/letter/content",
      "/api/letter/font/refresh",
      "/api/letter/home"
  };

  private static final String[] ALLOWED_ORIGINS = {
      "http://localhost:8081",
      "http://localhost:8080",
      "http://localhost:3000",
      "https://olllim-fe.vercel.app",
      "https://sandoll-sinhan.p-e.kr"

  };

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    List<String> origins = new ArrayList<>(Arrays.asList(ALLOWED_ORIGINS));
    origins.add(serverUrl);
    config.setAllowedOrigins(origins);
    config.setAllowedMethods(Collections.singletonList("*"));
    config.setAllowedHeaders(Collections.singletonList("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint() {
    return (request, response, authException) -> {
      ErrorStatus errorStatus = ErrorStatus.UNAUTHORIZED;
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding("UTF-8");
      response.setStatus(errorStatus.getHttpStatus().value());
      ResponseEntity<ApiResponse<Void>> errorResponse = ApiResponse.fail(ErrorStatus.UNAUTHORIZED);
      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    };
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .httpBasic(HttpBasicConfigurer::disable)
        .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(exceptionHandlingConfigurer ->
            exceptionHandlingConfigurer
                .authenticationEntryPoint(authenticationEntryPoint())
        )
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(PUBLIC_URLS).permitAll()
            .anyRequest().authenticated()
        )
        .oauth2Login(oauth -> oauth
            .authorizationEndpoint(authorization -> authorization
                .authorizationRequestResolver(
                    new CustomOAuth2AuthorizationRequestResolver(clientRegistrationRepository))
            )
            .successHandler(oAuthLoginSuccessHandler)
            .failureHandler(oAuthLoginFailureHandler)
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(rateLimitFilter, JwtFilter.class);

    return httpSecurity.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
