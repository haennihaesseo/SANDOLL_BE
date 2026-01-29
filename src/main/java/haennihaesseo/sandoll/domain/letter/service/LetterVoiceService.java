package haennihaesseo.sandoll.domain.letter.service;

import haennihaesseo.sandoll.domain.font.entity.Font;
import haennihaesseo.sandoll.domain.font.repository.FontRepository;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetter;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetterRepository;
import haennihaesseo.sandoll.domain.letter.converter.LetterVoiceConverter;
import haennihaesseo.sandoll.domain.letter.dto.response.VoiceAnalysisResponse;
import haennihaesseo.sandoll.domain.letter.exception.LetterException;
import haennihaesseo.sandoll.domain.letter.status.LetterErrorStatus;
import haennihaesseo.sandoll.global.infra.python.PythonAnalysisClient;
import haennihaesseo.sandoll.global.infra.python.dto.PythonVoiceAnalysisRequest;
import haennihaesseo.sandoll.global.infra.python.dto.PythonVoiceAnalysisResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LetterVoiceService {

  private final CachedLetterRepository cachedLetterRepository;
  private final PythonAnalysisClient pythonAnalysisClient;
  private final FontRepository fontRepository;
  private final LetterVoiceConverter letterVoiceConverter;

  public VoiceAnalysisResponse analyzeVoice(String letterId) {
    // Redis에서 CachedLetter 조회
    CachedLetter cachedLetter = cachedLetterRepository.findById(letterId)
        .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

    log.info("[분석 요청] letterId={}, content={}", letterId, cachedLetter.getContent());

    // 파이썬 서버에 분석 요청
    PythonVoiceAnalysisRequest request = letterVoiceConverter.toAnalysisRequest(cachedLetter);
    PythonVoiceAnalysisResponse pythonResponse = pythonAnalysisClient.requestVoiceAnalysis(request);

    log.info("[분석 완료] letterId={}, 추천 폰트: {}", letterId, pythonResponse.getRecommendedFonts());

    // 추천 폰트 정보 이름으로 매칭 후 응답 변환
    List<Font> recommendedFonts = fontRepository.findByNameIn(pythonResponse.getRecommendedFonts());

    return letterVoiceConverter.toVoiceAnalysisResponse(pythonResponse.getAnalysisResult(), recommendedFonts);
  }
}