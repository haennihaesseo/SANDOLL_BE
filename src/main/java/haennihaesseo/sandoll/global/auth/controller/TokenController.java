package haennihaesseo.sandoll.global.auth.controller;

import haennihaesseo.sandoll.global.auth.dto.TokenDto;
import haennihaesseo.sandoll.global.auth.service.TokenService;
import haennihaesseo.sandoll.global.response.ApiResponse;
import haennihaesseo.sandoll.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Token", description = "토큰 발급 및 재발급 API")
@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenController {

  private final TokenService tokenService;

  @Operation(
      summary = "[1.2] 임시키 기반 액세스 및 리프레쉬 토큰 발급"
  )
  @GetMapping
  public ResponseEntity<ApiResponse<TokenDto.TokenResponseDto>> issueToken(
      @Parameter(description = "카카오 로그인 후 발급받은 임시 키", required = true)
      @RequestHeader("tmpKey") String tmpKey
  ) {
    TokenDto.TokenResponseDto issueTokenResponse = tokenService.issueTokens(tmpKey);

    return ApiResponse.success(SuccessStatus.OK, issueTokenResponse);
  }

  @Operation(
      summary = "[1.3] 리프레쉬 기반 엑세스토큰 재발급"
  )
  @GetMapping("/reissue")
  public ResponseEntity<ApiResponse<TokenDto.ReissueResponseDto>> reissueAccessToken(
      @Parameter(description = "Refresh Token", required = true)
      @RequestHeader("refreshToken") String refreshToken
  ) {
    TokenDto.ReissueResponseDto accessToken = tokenService.reissueAccessToken(refreshToken);

    return ApiResponse.success(SuccessStatus.OK, accessToken);
  }

}