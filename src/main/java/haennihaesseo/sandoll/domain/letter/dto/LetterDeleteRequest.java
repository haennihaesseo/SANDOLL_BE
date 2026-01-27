package haennihaesseo.sandoll.domain.letter.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class LetterDeleteRequest {
    private LetterType type;
    private List<Long> letterIds;
}
