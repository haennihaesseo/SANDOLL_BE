package haennihaesseo.sandoll.domain.deco.status;

import haennihaesseo.sandoll.global.base.BaseSuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DecoSuccessStatus implements BaseSuccessStatus {

    SUCCESS_401(HttpStatus.OK, "SUCCESS_401", "편지지 조회에 성공했습니다."),
    SUCCESS_403(HttpStatus.CREATED, "SUCCESS_403", "bgm 생성을 시작합니다."),
    SUCCESS_404(HttpStatus.OK, "SUCCESS_404", "생성된 bgm을 조회했습니다."),
    SUCCESS_405(HttpStatus.OK, "SUCCESS_404", "bgm 저장에 성공했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
