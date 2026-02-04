package haennihaesseo.sandoll.domain.letter.service;

import haennihaesseo.sandoll.domain.deco.repository.TemplateRepository;
import haennihaesseo.sandoll.domain.font.entity.Font;
import haennihaesseo.sandoll.domain.font.repository.FontRepository;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetter;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetterRepository;
import haennihaesseo.sandoll.domain.letter.cache.CachedWord;
import haennihaesseo.sandoll.domain.letter.converter.LetterConverter;
import haennihaesseo.sandoll.domain.letter.dto.response.WritingLetterContentResponse;
import haennihaesseo.sandoll.domain.letter.dto.response.VoiceSaveResponse;
import haennihaesseo.sandoll.domain.letter.exception.LetterException;
import haennihaesseo.sandoll.domain.letter.status.LetterErrorStatus;
import haennihaesseo.sandoll.global.infra.AwsS3Client;
import haennihaesseo.sandoll.global.infra.stt.GoogleSttClient;
import haennihaesseo.sandoll.global.infra.stt.SttResult;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class LetterService {

  private final AwsS3Client s3Client;
  private final GoogleSttClient googleSttClient;
  private final CachedLetterRepository cachedLetterRepository;
  private final LetterConverter letterConverter;
  private final FontRepository fontRepository;

  /**
   * 음성 파일 저장 및 STT 편지 내용 조회, 편지 작성 키 발급
   * @param file
   * @return
   */
  public VoiceSaveResponse saveVoiceFile(MultipartFile file) {

    // 1. STT 처리 (S3 업로드 전에 처리)
    SttResult sttResult = googleSttClient.transcribe(file);

    // 10글자 이하일 경우 예외 처리
    if (sttResult.getFullText().replaceAll("\\s+", "").length() <= 10) {
      throw new LetterException(LetterErrorStatus.TOO_SHORT_CONTENT);
    }

    // 1000글자 초과일 경우 예외 처리
    if (sttResult.getFullText().length() > 1000) {
      throw new LetterException(LetterErrorStatus.TOO_LONG_CONTENT);
    }

    // 2. S3 업로드
    String fileUrl = s3Client.uploadFile("voice", file);

    // 3. Redis 저장
    String letterId = UUID.randomUUID().toString();
    List<CachedWord> cachedWords = letterConverter.toCachedWords(sttResult);
    CachedLetter cachedLetter = letterConverter.toCachedLetter(letterId, fileUrl, sttResult, cachedWords);
    cachedLetterRepository.save(cachedLetter);

    // 4. 응답 반환
    return letterConverter.toVoiceSaveResponse(letterId, fileUrl, sttResult);
  }

  public WritingLetterContentResponse getWritingLetterContent(String letterId) {
    // Redis에서 CachedLetter 조회
    CachedLetter cachedLetter = cachedLetterRepository.findById(letterId)
        .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

    // 폰트 이름 조회
    String fontName = null;
    if (cachedLetter.getFontId() != null) {
      fontName = fontRepository.findById(cachedLetter.getFontId())
          .map(Font::getName)
          .orElse(null);
    }

    return letterConverter.toWritingLetterContentResponse(cachedLetter, fontName);
  }
}

