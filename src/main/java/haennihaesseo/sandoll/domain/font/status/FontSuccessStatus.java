package haennihaesseo.sandoll.domain.font.status;

import haennihaesseo.sandoll.global.base.BaseSuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FontSuccessStatus implements BaseSuccessStatus {

  SUCCESS_304(HttpStatus.OK, "SUCCESS_304", "폰트 적용에 성공하였습니다."),
  SUCCESS_306(HttpStatus.OK, "SUCCESS_306", "추천 폰트 조회에 성공하였습니다."),
  SUCCESS_309(HttpStatus.OK, "SUCCESS_309", "추천 폰트 리스트 새로고침을 성공했습니다.")
  ;

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}