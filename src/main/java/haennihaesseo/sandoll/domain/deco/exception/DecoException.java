package haennihaesseo.sandoll.domain.deco.exception;

import haennihaesseo.sandoll.global.base.BaseErrorStatus;
import haennihaesseo.sandoll.global.exception.GlobalException;
import lombok.Getter;

@Getter
public class DecoException extends GlobalException {
    public DecoException(BaseErrorStatus errorStatus) {
        super(errorStatus);
    }
}
