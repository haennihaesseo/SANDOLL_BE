package haennihaesseo.sandoll.global.infra.stt;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SttWord {

    private final String word;
    private final Double startTime;
    private final Double endTime;
    private final Double order;
}