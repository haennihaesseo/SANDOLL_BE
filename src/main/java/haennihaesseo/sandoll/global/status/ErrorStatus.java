package haennihaesseo.sandoll.global.status;

import haennihaesseo.sandoll.global.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorStatus {

  /**
   *  Error Code
   *  400 : 잘못된 요청
   *  401 : JWT에 대한 오류
   *  403 : 요청한 정보에 대한 권한 없음.
   *  404 : 존재하지 않는 정보에 대한 요청.
   *  405 : 허용되지 않은 메소드입니다.
   *  409 : 데이터 무결성 위반(중복 등)
   */

  BAD_REQUEST(HttpStatus.BAD_REQUEST, "400", "잘못된 요청입니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "401", "인증이 필요합니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN, "403", "접근 권한이 없습니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "404", "요청한 자원을 찾을 수 없습니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "405", "허용되지 않은 메소드입니다."),
  CONFLICT(HttpStatus.CONFLICT, "409", "이미 사용 중인 값입니다."),



  /**
   *  Error Code
   *  500 : 서버 내부 오류
   */
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500", "서버 내부 오류입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
