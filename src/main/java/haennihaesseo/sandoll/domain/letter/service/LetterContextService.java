package haennihaesseo.sandoll.domain.letter.service;

import com.fasterxml.jackson.databind.JsonNode;
import haennihaesseo.sandoll.domain.font.service.FontContextRecommendService;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetter;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetterRepository;
import haennihaesseo.sandoll.domain.letter.exception.LetterException;
import haennihaesseo.sandoll.domain.letter.status.LetterErrorStatus;
import haennihaesseo.sandoll.global.infra.python.PythonAnalysisClient;
import haennihaesseo.sandoll.global.infra.python.dto.ContextAnalysisRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LetterContextService {
    private final CachedLetterRepository cachedLetterRepository;
    private final PythonAnalysisClient pythonAnalysisClient;
    private final FontContextRecommendService fontContextRecommendService;

    public void contextAnalyze(String letterId){
        CachedLetter cachedLetter = cachedLetterRepository.findById(letterId)
                .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

        ContextAnalysisRequest request = new ContextAnalysisRequest(cachedLetter.getContent(), 1); // todo count=3 수정예정

        // todo error 코드 추가 예정
        pythonAnalysisClient.requestContextAnalysis(request)
                .subscribe(event -> {
                    if ("analyze".equals(event.getStep())) {
                        // 분석 결과 처리
                        JsonNode analysis = event.getData().get("analysis");
                        // 1. 바탕으로 폰트 추천 알고리즘 구축 FontRecommendService 및 캐시에 저장
                        fontContextRecommendService.saveContextFontsInLetter(letterId, analysis);
                    } else if ("done".equals(event.getStep())) {
                        // BGM 결과 처리
                        JsonNode bgmList = event.getData().get("bgmList");
                        // 1. bgm 결과 redis에 저장
                    }
                });
    }
}
