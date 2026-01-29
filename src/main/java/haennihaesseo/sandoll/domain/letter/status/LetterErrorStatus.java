package haennihaesseo.sandoll.domain.letter.status;

import haennihaesseo.sandoll.global.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LetterErrorStatus implements BaseErrorStatus {

    LETTER_NOT_FOUND(HttpStatus.NOT_FOUND, "LETTER_NOT_FOUND", "해당 편지가 존재하지 않습니다."),
    NOT_LETTER_OWNER(HttpStatus.FORBIDDEN, "NOT_LETTER_OWNER", "편지 삭제 권한이 없습니다."),
    TOO_SHORT_CONTENT(HttpStatus.BAD_REQUEST, "TOO_SHORT_CONTENT", "편지 내용이 너무 짧습니다. 최소 10글자 이상 작성해주세요."),


    LETTER_NEED_PASSWORD(HttpStatus.UNAUTHORIZED, "LETTER_NEED_PASSWORD", "비밀번호가 필요합니다."),
    LETTER_WRONG_PASSWORD(HttpStatus.FORBIDDEN, "LETTER_WRONG_PASSWORD", "비밀번호가 옳지 않습니다. 다시 시도해주세요"),

    NOT_OWN_LETTER(HttpStatus.FORBIDDEN, "NOT_OWN_LETTER", "편지 소유자가 아닙니다"),
    LETTER_ENCRYPT_FAILED(HttpStatus.BAD_REQUEST, "LETTER_ENCRYPT_FAILED", "편지 링크가 생성되지 못했습니다."),
    LETTER_DECRYPT_FAILED(HttpStatus.BAD_REQUEST, "LETTER_DECRYPT_FAILED", "편지 링크가 잘못되었습니다."),

    CANNOT_SAVE_OWN_LETTER(HttpStatus.BAD_REQUEST, "CANNOT_SAVE_OWN_LETTER", "자신의 편지는 보관함에 저장할 수 없습니다."),
    ALREADY_SAVE_LETTER(HttpStatus.BAD_REQUEST, "ALREADY_SAVE_LETTER", "이미 보관함에 저장한 편지입니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
