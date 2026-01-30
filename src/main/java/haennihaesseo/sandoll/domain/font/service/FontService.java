package haennihaesseo.sandoll.domain.font.service;

import haennihaesseo.sandoll.domain.font.exception.FontException;
import haennihaesseo.sandoll.domain.font.repository.FontRepository;
import haennihaesseo.sandoll.domain.font.status.FontErrorStatus;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetter;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetterRepository;
import haennihaesseo.sandoll.domain.letter.exception.LetterException;
import haennihaesseo.sandoll.domain.letter.status.LetterErrorStatus;
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

  /**
   * 폰트 적용
   * @param letterId
   * @param fontId
   */
  @Transactional
  public void applyFont(String letterId, Long fontId) {
    // Redis에서 CachedLetter 조회
    CachedLetter cachedLetter = cachedLetterRepository.findById(letterId)
        .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

    // 폰트 존재 여부 확인
    if (!fontRepository.existsById(fontId)) {
      throw new FontException(FontErrorStatus.FONT_NOT_FOUND);
    }

    // 폰트 적용
    cachedLetter.setFontId(fontId);
    cachedLetterRepository.save(cachedLetter);
  }

}
