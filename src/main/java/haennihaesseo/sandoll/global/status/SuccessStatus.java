package haennihaesseo.sandoll.global.status;

import haennihaesseo.sandoll.global.base.BaseSuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseSuccessStatus {

  // 전역
  OK(HttpStatus.OK, "200", "요청이 성공적으로 처리되었습니다."),
  CREATED(HttpStatus.CREATED, "201", "리소스가 성공적으로 생성되었습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
