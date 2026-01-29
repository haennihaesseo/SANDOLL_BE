package haennihaesseo.sandoll.global.infra.stt;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SttResult {

    private final String fullText;
    private final Integer totalDuration;
    private final List<SttWord> words;
}