package haennihaesseo.sandoll.global.infra.python;

import haennihaesseo.sandoll.global.infra.python.dto.PythonVoiceAnalysisRequest;
import haennihaesseo.sandoll.global.infra.python.dto.PythonVoiceAnalysisResponse;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class PythonAnalysisClient {

    private final WebClient webClient;

    public PythonAnalysisClient(@Value("${external.python-server-url}") String pythonServerUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(pythonServerUrl)
                .build();
    }

    public PythonVoiceAnalysisResponse requestVoiceAnalysis(PythonVoiceAnalysisRequest request) {
        log.info("[Python] 분석 요청 - voiceUrl: {}, 단어 수: {}", request.getVoiceUrl(), request.getWords().size());

        PythonVoiceAnalysisResponse response = webClient.post()
                .uri("/api/voice")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PythonVoiceAnalysisResponse.class)
                .block(Duration.ofSeconds(20)); // 최대 20초 대기, 추후 테스트 후 조정할 예정

        log.info("[Python] 분석 완료 - 추천 폰트: {}, 분석 결과: {}",
                response != null ? response.getRecommendedFonts() : "null",
                response != null ? response.getAnalysisResult() : "null");

        return response;
    }
}