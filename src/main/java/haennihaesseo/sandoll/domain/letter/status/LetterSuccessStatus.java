package haennihaesseo.sandoll.domain.letter.status;

import haennihaesseo.sandoll.global.base.BaseErrorStatus;
import haennihaesseo.sandoll.global.base.BaseSuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LetterSuccessStatus implements BaseSuccessStatus {

    SUCCESS_201(HttpStatus.OK, "SUCCESS_201", "받은 편지 전체 리스트를 조회했습니다."),
    SUCCESS_202(HttpStatus.OK, "SUCCESS_202", "편지 조회에 성공했습니다."),
    SUCCESS_203(HttpStatus.OK, "SUCCESS_203", "편지 숨김 처리를 완료하였습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
