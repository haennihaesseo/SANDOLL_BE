package haennihaesseo.sandoll.domain.letter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import haennihaesseo.sandoll.domain.deco.dto.response.BgmsResponse;
import haennihaesseo.sandoll.domain.deco.service.BgmService;
import haennihaesseo.sandoll.domain.font.service.FontContextRecommendService;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetter;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetterRepository;
import haennihaesseo.sandoll.domain.letter.converter.LetterConverter;
import haennihaesseo.sandoll.domain.letter.exception.LetterException;
import haennihaesseo.sandoll.domain.letter.status.LetterErrorStatus;
import haennihaesseo.sandoll.global.infra.python.PythonAnalysisClient;
import haennihaesseo.sandoll.global.infra.python.dto.ContextAnalysisRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LetterContextService {
    private final CachedLetterRepository cachedLetterRepository;
    private final PythonAnalysisClient pythonAnalysisClient;
    private final FontContextRecommendService fontContextRecommendService;
    private final LetterConverter letterConverter;
    private final BgmService bgmService;

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
                        // 바탕으로 폰트 추천 알고리즘 구축 FontRecommendService 및 캐시에 저장
                        fontContextRecommendService.saveContextFontsInLetter(letterId, analysis);
                    } else if ("done".equals(event.getStep())) {
                        JsonNode bgms = event.getData().get("bgms");
                        // bgm 결과 redis에 저장
                        List<BgmsResponse.BgmDto> bgmDtos = letterConverter.toBgmDtos(bgms);
                        try {
                            bgmService.saveBgmsAtRedis(letterId, bgmDtos);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }
}
