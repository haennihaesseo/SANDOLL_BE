package haennihaesseo.sandoll.domain.deco.status;

import haennihaesseo.sandoll.global.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DecoErrorStatus implements BaseErrorStatus {

    BGM_GENERATING(HttpStatus.BAD_REQUEST, "BGM_GENERATING", "현재 배경음악을 생성 중입니다. 잠시 후 다시 확인해주세요.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
