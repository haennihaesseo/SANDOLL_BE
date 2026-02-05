package haennihaesseo.sandoll.global.auth.service;

import haennihaesseo.sandoll.domain.user.entity.User;
import haennihaesseo.sandoll.domain.user.repository.UserRepository;
import haennihaesseo.sandoll.global.auth.dto.TokenDto;
import haennihaesseo.sandoll.global.auth.util.JwtUtil;
import haennihaesseo.sandoll.global.exception.GlobalException;
import haennihaesseo.sandoll.global.infra.RedisClient;
import haennihaesseo.sandoll.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

  private final RedisClient redisClient;
  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;

  @Value("${jwt.refresh-expiration-ms}")
  private long REFRESH_TOKEN_EXPIRATION_MS;

  /**
   * 임시 키을 이용하여 AccessToken과 RefreshToken을 발급한다.
   * @param tmpKey
   * @return
   */
  public TokenDto.TokenResponseDto issueTokens(String tmpKey) {
    // 임시 토큰 유효성 검증
    jwtUtil.validateTmpKey(tmpKey);
    Long userId = jwtUtil.getUserIdFromTmpKey(tmpKey);

    // 유저 조회
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new GlobalException(ErrorStatus.NOT_FOUND));

    // 액세스 토큰 및 리프레시 토큰 발급
    String accessToken = jwtUtil.generateAccessToken(user.getUserId());

    // 이미 리프레쉬토큰 있으면 삭제 후 발급
    redisClient.deleteData("REFRESH_TOKEN_", userId.toString());

    String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());
    redisClient.setData("REFRESH_TOKEN_", userId.toString(), refreshToken, REFRESH_TOKEN_EXPIRATION_MS);

    return TokenDto.TokenResponseDto.builder()
        .userId(userId)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }


  /**
   * RefreshToken을 이용하여 새로운 AccessToken을 발급한다.
   * @param refreshToken
   * @return
   */
  public TokenDto.ReissueResponseDto reissueAccessToken(String refreshToken) {
    jwtUtil.validateRefreshToken(refreshToken);
    Long userId = jwtUtil.getUserIdFromRefreshToken(refreshToken);

    return TokenDto.ReissueResponseDto.builder()
        .accessToken(jwtUtil.generateAccessToken(userId))
        .build();
  }

  public void logout(Long userId) {
    redisClient.deleteData("REFRESH_TOKEN_", userId.toString());
  }

}
