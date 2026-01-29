package haennihaesseo.sandoll.domain.letter.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class LetterDeleteRequest {

    @NotNull
    private LetterType type;

    @NotEmpty(message = "삭제하려는 편지를 선택해주세요.")
    private List<Long> letterIds;
}
