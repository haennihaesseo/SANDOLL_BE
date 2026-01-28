package haennihaesseo.sandoll.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
  @Value("${app.server-url}")
  private String SERVER_URL;

  @Bean
  public OpenAPI openAPI() {

    // API 기본 정보 설정
    Info info = new Info()
        .title("올림 API Document")
        .version("1.0")
        .description("환영합니다! 이 API 문서는 올림의 API를 사용하는 방법을 설명합니다.\n")
        .contact(new Contact().email("haennihaesseo@gmail.com"));

    // JWT 인증 방식 설정
    String jwtScheme = "jwtAuth";
    SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtScheme);
    Components components = new Components()
        .addSecuritySchemes(jwtScheme, new SecurityScheme()
            .name("Authorization")
            .type(SecurityScheme.Type.HTTP)
            .in(SecurityScheme.In.HEADER)
            .scheme("bearer")
            .bearerFormat("JWT"));

    // Swagger UI 설정 및 보안 추가
    return new OpenAPI()
        .addServersItem(new Server().url(SERVER_URL))
        .components(components)
        .info(info)
        .addSecurityItem(securityRequirement);
  }
}