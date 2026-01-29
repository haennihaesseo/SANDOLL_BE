package haennihaesseo.sandoll.global.infra.python.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PythonVoiceAnalysisResponse {

    private String analysisResult;
    private List<String> recommendedFonts;
}