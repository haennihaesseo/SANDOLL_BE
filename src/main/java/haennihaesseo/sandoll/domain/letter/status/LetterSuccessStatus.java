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
    SUCCESS_203(HttpStatus.OK, "SUCCESS_203", "편지 숨김 처리를 완료하였습니다."),
    SUCCESS_204(HttpStatus.OK, "SUCCESS_204", "보낸 편지 전체 리스트를 조회했습니다."),
    SUCCESS_205(HttpStatus.OK, "SUCCESS_205", "편지 아이디를 재발급했습니다."),

    SUCCESS_301(HttpStatus.CREATED, "SUCCESS_301", "음성 업로드에 성공하였습니다."),
    SUCCESS_302(HttpStatus.OK, "SUCCESS_302", "편지 정보 입력에 성공했습니다."),
    SUCCESS_303(HttpStatus.OK, "SUCCESS_303", "목소리 분석에 성공하였습니다."),
    SUCCESS_305(HttpStatus.OK, "SUCCESS_305", "편지 조회에 성공하였습니다."),

    SUCCESS_501(HttpStatus.CREATED, "SUCCESS_501", "편지 아이디를 발급했습니다."),
    SUCCESS_502(HttpStatus.OK, "SUCCESS_502","편지 비밀번호 설정이 완료되었습니다."),
    SUCCESS_503(HttpStatus.OK, "SUCCESS_503", "편지 조회에 성공했습니다."),

    SUCCESS_601(HttpStatus.OK, "SUCCESS_601", "편지 조회에 성공했습니다."),
    SUCCESS_602(HttpStatus.CREATED, "SUCCESS_602", "편지를 보관함에 성공적으로 저장했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
