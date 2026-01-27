package haennihaesseo.sandoll.domain.letter.status;

import haennihaesseo.sandoll.global.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LetterErrorStatus implements BaseErrorStatus {

    LETTER_NOT_FOUND(HttpStatus.NOT_FOUND, "LETTER_NOT_FOUND", "해당 편지가 존재하지 않습니다."),
    NOT_LETTER_OWNER(HttpStatus.FORBIDDEN, "NOT_LETTER_OWNER", "편지 삭제 권한이 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
