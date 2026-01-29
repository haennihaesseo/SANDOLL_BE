package haennihaesseo.sandoll.global.infra.python.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PythonVoiceAnalysisRequest {

    private String voiceUrl;
    private String content;
    private List<WordData> words;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class WordData {
        private String word;
        private Double startTime;
        private Double endTime;
    }
}