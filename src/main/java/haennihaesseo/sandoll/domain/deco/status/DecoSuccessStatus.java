package haennihaesseo.sandoll.domain.deco.status;

import haennihaesseo.sandoll.global.base.BaseSuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DecoSuccessStatus implements BaseSuccessStatus {

    SUCCESS_401(HttpStatus.OK, "SUCCESS_401", "편지지 조회에 성공했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
