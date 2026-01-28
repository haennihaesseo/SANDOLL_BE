package haennihaesseo.sandoll.global.auth.util;

import haennihaesseo.sandoll.global.auth.status.TokenErrorStatus;
import haennihaesseo.sandoll.global.exception.GlobalException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

  @Value("${jwt.access-secret}")
  private String ACCESS_SECRET_KEY;

  @Value("${jwt.refresh-secret}")
  private String REFRESH_SECRET_KEY;

  @Value("${jwt.tmp-secret}")
  private String TMP_KEY_SECRET;

  @Value("${jwt.access-expiration-ms}")
  private long ACCESS_TOKEN_EXPIRATION_MS;

  @Value("${jwt.refresh-expiration-ms}")
  private long REFRESH_TOKEN_EXPIRATION_MS;

  @Value("${jwt.tmp-expiration-ms}")
  private long TMP_KEY_EXPIRATION_MS;

  private SecretKey accessKey;
  private SecretKey refreshKey;
  private SecretKey tmpKey;

  @PostConstruct
  public void init() {
    if (ACCESS_SECRET_KEY == null || REFRESH_SECRET_KEY == null) {
      throw new JwtException("jwt 비밀키가 존재하지 않습니다.");
    }
    this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(ACCESS_SECRET_KEY));
    this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(REFRESH_SECRET_KEY));
    this.tmpKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TMP_KEY_SECRET));
  }

  // == 토큰 생성 메서드 == //

  /* 액세스 토큰 생성 */
  public String generateAccessToken(Long userId) {
    Date now = new Date();
    Date expires = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_MS);
    return Jwts.builder()
        .subject(userId.toString())
        .expiration(expires)
        .issuedAt(now)
        .signWith(accessKey)
        .compact();
  }

  /* 리프레시 토큰 생성 */
  public String generateRefreshToken(Long userId) {
    Date now = new Date();
    Date expires = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_MS);
    return Jwts.builder()
        .subject(userId.toString())
        .expiration(expires)
        .issuedAt(now)
        .signWith(refreshKey)
        .compact();
  }

  /* 임시 키 생성 */
  public String generateTmpKey(Long userId) {
    Date now = new Date();
    Date expires = new Date(now.getTime() + TMP_KEY_EXPIRATION_MS);
    return Jwts.builder()
        .subject(userId.toString())
        .expiration(expires)
        .issuedAt(now)
        .signWith(tmpKey)
        .compact();
  }

  // == 토큰 검증 메서드 == //

  /* 액세스 토큰 유효성 검증 */
  public void validateAccessToken(String token) {
    try {
      Jwts.parser()
          .verifyWith(accessKey).build().parseSignedClaims(token).getPayload();
    } catch (ExpiredJwtException e) {
      log.warn("만료된 액세스 토큰: {}", e.getMessage());
      throw new GlobalException(TokenErrorStatus.INVALID_ACCESS_TOKEN);
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("유효하지 않은 액세스 토큰: {}", e.getMessage());
      throw new GlobalException(TokenErrorStatus.INVALID_ACCESS_TOKEN);
    }
  }

  /* 리프레시 토큰 유효성 검증 */
  public void validateRefreshToken(String token) {
    try {
      Jwts.parser()
          .verifyWith(refreshKey).build().parseSignedClaims(token).getPayload();
    } catch (ExpiredJwtException e) {
      log.warn("만료된 리프레시 토큰: {}", e.getMessage());
      throw new GlobalException(TokenErrorStatus.INVALID_REFRESH_TOKEN);
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("유효하지 않은 리프레시 토큰: {}", e.getMessage());
      throw new GlobalException(TokenErrorStatus.INVALID_REFRESH_TOKEN);
    }
  }

  /* 임시 키 유효성 검증 */
  public void validateTmpKey(String token) {
    try {
      Jwts.parser()
          .verifyWith(tmpKey).build().parseSignedClaims(token).getPayload();
    } catch (ExpiredJwtException e) {
      log.warn("만료된 임시 키: {}", e.getMessage());
      throw new GlobalException(TokenErrorStatus.INVALID_TMP_KEY);
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("유효하지 않은 임시 키: {}", e.getMessage());
      throw new GlobalException(TokenErrorStatus.INVALID_TMP_KEY);
    }
  }

  // == 토큰에서 정보 추출 메서드 == //

  /* 액세스 토큰에서 사용자 ID 추출 */
  public Long getUserIdFromAccessToken(String token) {
    return Long.parseLong(
        Jwts.parser()
            .verifyWith(accessKey).build().parseSignedClaims(token).getPayload().getSubject()
    );
  }

  /* 리프레시 토큰에서 사용자 ID 추출 */
  public Long getUserIdFromRefreshToken(String token) {
    return Long.parseLong(
        Jwts.parser()
            .verifyWith(refreshKey).build().parseSignedClaims(token).getPayload().getSubject()
    );
  }

  /* 임시 키에서 사용자 ID 추출 */
  public Long getUserIdFromTmpKey(String token) {
    return Long.parseLong(
        Jwts.parser()
            .verifyWith(tmpKey).build().parseSignedClaims(token).getPayload().getSubject()
    );
  }

}
