package haennihaesseo.sandoll.domain.letter.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LetterPasswordRequest {

    @NotBlank(message = "편지 비밀키는 필수입니다.")
    @Schema(description = "편지 비밀키", example = "UNk/Z/JAPhtCOyloN2DJaw==", requiredMode = Schema.RequiredMode.REQUIRED)
    private String secretLetterKey;

    @NotBlank(message = "비밀번호 설정시 비밀번호는 필수입니다.")
    @Schema(description = "편지 비밀번호", example = "1234", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
