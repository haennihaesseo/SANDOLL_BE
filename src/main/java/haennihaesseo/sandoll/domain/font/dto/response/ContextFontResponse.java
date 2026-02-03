package haennihaesseo.sandoll.domain.font.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@AllArgsConstructor
public class ContextFontResponse implements Serializable {
    private Long fontId;
    private List<String> keywords;
}
