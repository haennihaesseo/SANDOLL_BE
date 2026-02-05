package haennihaesseo.sandoll.domain.letter.service;

import haennihaesseo.sandoll.domain.font.entity.Font;
import haennihaesseo.sandoll.domain.font.repository.FontRepository;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetter;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetterRepository;
import haennihaesseo.sandoll.domain.letter.converter.LetterConverter;
import haennihaesseo.sandoll.domain.letter.dto.response.VoiceAnalysisResponse;
import haennihaesseo.sandoll.domain.letter.exception.LetterException;
import haennihaesseo.sandoll.domain.letter.status.LetterErrorStatus;
import haennihaesseo.sandoll.global.infra.RedisClient;
import haennihaesseo.sandoll.global.infra.python.PythonAnalysisClient;
import haennihaesseo.sandoll.global.infra.python.dto.PythonVoiceAnalysisRequest;
import haennihaesseo.sandoll.global.infra.python.dto.PythonVoiceAnalysisResponse;
import haennihaesseo.sandoll.global.status.ErrorStatus;
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
  private final LetterConverter letterConverter;
  private final RedisClient redisClient;

  private final int RECOMMEND_FONT_COUNT = 3;
  private final long TIMEOUT_MS = 180000; // 3분

  public VoiceAnalysisResponse analyzeVoice(String letterId) {
    // Redis에서 CachedLetter 조회
    CachedLetter cachedLetter = cachedLetterRepository.findById(letterId)
        .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

    //이미 분석 완료
    if (cachedLetter.getVoiceFontIds() != null && !cachedLetter.getVoiceFontIds().isEmpty()) {
      throw new LetterException(LetterErrorStatus.ALREADY_ANALYZED);
    }

    // 락 획득
    boolean acquired = redisClient.acquireLock("VOICE_ANALYSIS_LOCK:" + letterId, "ANALYZING", TIMEOUT_MS);
    if(!acquired) {
      throw new LetterException(LetterErrorStatus.ALREADY_ANALYZED);
    }

    try {
      // 파이썬 서버에 분석 요청
      PythonVoiceAnalysisRequest request = letterConverter.toAnalysisRequest(cachedLetter);
      PythonVoiceAnalysisResponse pythonResponse = pythonAnalysisClient.requestVoiceAnalysis(request);

      if (pythonResponse == null || pythonResponse.getRecommendedFonts() == null) {
        log.error("파이썬 분석 결과 없음 letterId={}", letterId);
        throw new LetterException(ErrorStatus.PYTHON_SERVER_ERROR);
      }

      log.info("[분석 완료] letterId={}, 추천 폰트: {}", letterId, pythonResponse.getRecommendedFonts());

      // 추천 폰트 정보 이름으로 매칭
      List<Font> recommendedFonts = fontRepository.findByNameIn(pythonResponse.getRecommendedFonts());

      // 분석된 폰트 결과 저장
      cachedLetter.setVoiceFonts(recommendedFonts, pythonResponse.getVoiceKeywords());

      // 앞에 3개 폰트만 추천으로 설정
      if (recommendedFonts.size() > RECOMMEND_FONT_COUNT) {
        recommendedFonts = recommendedFonts.subList(0, RECOMMEND_FONT_COUNT);
      }

      // 현재 추천된 폰트 정보 캐쉬에 저장
      cachedLetter.setCurrentRecommendFontIds(recommendedFonts.stream().map(Font::getFontId).toList());
      // shown 폰트 업데이트
      cachedLetter.setShownVoiceFontIds(recommendedFonts.stream().map(Font::getFontId).toList());

      cachedLetterRepository.save(cachedLetter);

      return letterConverter.toVoiceAnalysisResponse(pythonResponse.getAnalysisResult(), recommendedFonts);
    } finally {
      // 락 해제
      redisClient.releaseLock("VOICE_ANALYSIS_LOCK:" + letterId);
    }
  }
}