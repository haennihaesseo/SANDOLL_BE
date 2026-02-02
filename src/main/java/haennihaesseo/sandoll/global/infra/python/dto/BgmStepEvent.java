package haennihaesseo.sandoll.global.infra.python.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class BgmStepEvent {
    private String step;
    private JsonNode data;
    private String errorType;  // 추가
    private String message;
}