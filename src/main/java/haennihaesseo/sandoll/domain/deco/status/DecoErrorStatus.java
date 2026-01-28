package haennihaesseo.sandoll.domain.deco.status;

import haennihaesseo.sandoll.global.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DecoErrorStatus implements BaseErrorStatus {


    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
