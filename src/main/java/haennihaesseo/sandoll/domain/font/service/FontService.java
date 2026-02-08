package haennihaesseo.sandoll.domain.font.service;


import haennihaesseo.sandoll.domain.deco.entity.Template;
import haennihaesseo.sandoll.domain.deco.entity.enums.Size;
import haennihaesseo.sandoll.domain.deco.repository.TemplateRepository;
import haennihaesseo.sandoll.domain.font.dto.response.ContextFontResponse;
import haennihaesseo.sandoll.domain.font.dto.response.RecommendFontResponse;
import haennihaesseo.sandoll.domain.font.dto.response.RefreshFontResponse;
import haennihaesseo.sandoll.domain.font.dto.response.RefreshFontResponse.RecommendFont;
import haennihaesseo.sandoll.domain.font.entity.Font;
import haennihaesseo.sandoll.domain.font.entity.enums.FontType;
import haennihaesseo.sandoll.domain.font.exception.FontException;
import haennihaesseo.sandoll.domain.font.converter.FontConverter;
import haennihaesseo.sandoll.domain.font.repository.FontRepository;
import haennihaesseo.sandoll.domain.font.status.FontErrorStatus;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetter;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetterRepository;
import haennihaesseo.sandoll.domain.letter.exception.LetterException;
import haennihaesseo.sandoll.domain.letter.status.LetterErrorStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FontService {

  private final CachedLetterRepository cachedLetterRepository;
  private final FontRepository fontRepository;
  private final FontConverter fontConverter;
  private final TemplateRepository templateRepository;

  private final int SELECT_COUNT = 3; // 한 번에 추천할 폰트 개수
  private final int ONE_LINE_WORD_COUNT = 20; // 한 줄에 들어가는 글자 수 기준

  /**
   * 폰트 적용
   * @param letterId
   * @param fontId
   */
  public void applyFont(String letterId, Long fontId) {
    // Redis에서 CachedLetter 조회
    CachedLetter cachedLetter = cachedLetterRepository.findById(letterId)
        .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

    // 폰트 존재 여부 확인
    Font font = fontRepository.findById(fontId)
        .orElseThrow(() -> new FontException(FontErrorStatus.FONT_NOT_FOUND));

    // 폰트 적용
    cachedLetter.setFont(font.getFontId(), font.getFontUrl());

    // 글자수 세기 -> 전체 글자수 + \n은 20자로 간주 (바뀔 수 있음)
    int charCount = 0;
    String content = cachedLetter.getContent();
    for (char c : content.toCharArray()) {
      if (c == '\n') {
        charCount += ONE_LINE_WORD_COUNT;
      } else {
        charCount += 1;
      }
    }

    Size size = Size.fromLength(charCount);
    Template setTemplate = templateRepository.findByNameAndSize("무지", size); // Default인 무지로 설정
    cachedLetter.setTemplateId(setTemplate.getTemplateId());
    cachedLetter.setTemplateUrl(setTemplate.getImageUrl());
    cachedLetterRepository.save(cachedLetter);
  }

  /**
   * 추천 폰트 조회
   * @param letterId
   * @return RecommendFontResponse
   */
  public RecommendFontResponse getRecommendFonts(String letterId) {
    // Redis에서 CachedLetter 조회
    CachedLetter cachedLetter = cachedLetterRepository.findById(letterId)
        .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

    List<Font> voiceFonts = fontRepository.findAllByFontIdIn(cachedLetter.getCurrentRecommendFontIds());
    List<RecommendFont> voiceResponse = fontConverter.toRecommendFontList(voiceFonts, cachedLetter.getVoiceFontKeywords());

    List<ContextFontResponse> contextFontResponses = cachedLetter.getContextFonts();

    if (contextFontResponses == null)
      throw new FontException(FontErrorStatus.FONT_RECOMMENDATION_IN_PROGRESS);

    List<RecommendFont> contextResponse = contextFontResponses.stream()
            .map(cf ->{
              Font font = fontRepository.findById(cf.getFontId())
                      .orElseThrow(() -> new FontException(FontErrorStatus.FONT_NOT_FOUND));
              return new RecommendFont(
                      font.getFontId(),
                      font.getName(),
                      font.getFontUrl(),
                      cf.getKeywords()
              );
            })
            .toList();

    return fontConverter.toRecommendFontResponse(voiceResponse, contextResponse);
  }

  /**
   * 추천 폰트 새로고침
   * @param letterId
   * @return RecommendFontResponse
   */
  public RefreshFontResponse refreshRecommendFonts(String letterId) {
    // Redis에서 CachedLetter 조회
    CachedLetter cachedLetter = cachedLetterRepository.findById(letterId)
        .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

    List<Long> recommendedFontIds = cachedLetter.getVoiceFontIds();
    List<Long> shownFontIds = cachedLetter.getShownVoiceFontIds();

    // 추천 폰트 중에서 아직 보여주지 않은 폰트 필터링
    List<Long> availableFontIds = new ArrayList<>(recommendedFontIds.stream()
        .filter(fontId -> !shownFontIds.contains(fontId))
        .toList());

    List<Long> newRecommendedFontIds;

    if (availableFontIds.size() >= SELECT_COUNT) {
      // 충분한 폰트가 남아있으면 랜덤으로 선택
      Collections.shuffle(availableFontIds, ThreadLocalRandom.current());
      newRecommendedFontIds = new ArrayList<>(availableFontIds.subList(0, SELECT_COUNT));
    } else {
      // 남은 폰트를 모두 포함하고, shown 초기화 후 나머지를 채움
      newRecommendedFontIds = new ArrayList<>(availableFontIds);
      shownFontIds.clear();

      List<Long> refill = new ArrayList<>(recommendedFontIds.stream()
          .filter(fontId -> !newRecommendedFontIds.contains(fontId))
          .toList());
      Collections.shuffle(refill, ThreadLocalRandom.current());
      newRecommendedFontIds.addAll(refill.subList(0, Math.min(SELECT_COUNT - newRecommendedFontIds.size(), refill.size())));
    }

    // shown 리스트에 새로 추천된 폰트 반영
    shownFontIds.addAll(newRecommendedFontIds);
    // currentRecommendFontIds 업데이트
    cachedLetter.setCurrentRecommendFontIds(newRecommendedFontIds);
    cachedLetterRepository.save(cachedLetter);


    List<Font> fonts = fontRepository.findAllByFontIdIn(newRecommendedFontIds);
    return fontConverter.toRefreshFontResponse(FontType.VOICE, fonts,
        cachedLetter.getVoiceFontKeywords());
  }
}
