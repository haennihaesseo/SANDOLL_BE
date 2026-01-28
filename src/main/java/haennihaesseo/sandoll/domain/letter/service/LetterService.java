package haennihaesseo.sandoll.domain.letter.service;

import haennihaesseo.sandoll.domain.letter.cache.CachedLetter;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetterRepository;
import haennihaesseo.sandoll.domain.letter.cache.CachedWord;
import haennihaesseo.sandoll.domain.letter.dto.request.LetterInfoRequest;
import haennihaesseo.sandoll.domain.letter.dto.response.VoiceSaveResponse;
import haennihaesseo.sandoll.domain.letter.exception.LetterException;
import haennihaesseo.sandoll.domain.letter.status.LetterErrorStatus;
import haennihaesseo.sandoll.global.infra.AwsS3Client;
import haennihaesseo.sandoll.global.infra.stt.GoogleSttClient;
import haennihaesseo.sandoll.global.infra.stt.SttResult;
import java.util.ArrayList;
import java.util.Arrays;
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

  /**
   * 음성 파일 저장 및 STT 편지 내용 조회, 편지 작성 키 발급
   * @param file
   * @return
   */
  public VoiceSaveResponse saveVoiceFile(MultipartFile file) {

    // 1. STT 처리 (S3 업로드 전에 처리)
    SttResult sttResult = googleSttClient.transcribe(file);

    // 2. S3 업로드
    String fileUrl = s3Client.uploadFile("voice", file);

    // 3. CachedWord 변환
    List<CachedWord> cachedWords = sttResult.getWords().stream()
        .map(w -> CachedWord.builder()
            .word(w.getWord())
            .startTime(w.getStartTime())
            .endTime(w.getEndTime())
            .wordOrder((double) w.getOrder())
            .build())
        .toList();

    // 4. Redis 저장
    String letterId = UUID.randomUUID().toString();
    String letterKey = UUID.randomUUID().toString();
    CachedLetter cachedLetter = CachedLetter.builder()
        .letterId(letterId)
        .letterKey(letterKey)
        .voiceUrl(fileUrl)
        .duration(sttResult.getTotalDuration().intValue())
        .content(sttResult.getFullText())
        .words(cachedWords)
        .build();

    cachedLetterRepository.save(cachedLetter);

    // 5. 응답 반환
    return VoiceSaveResponse.builder()
        .letterId(letterId)
        .letterKey(letterKey)
        .voiceUrl(fileUrl)
        .duration(sttResult.getTotalDuration().intValue())
        .content(sttResult.getFullText())
        .build();
  }

  /**
   * 편지 정보 입력 및 내용 수정
   * @param letterId
   * @param letterKey
   * @param request
   */
  public void inputLetterInfo(String letterId, String letterKey, LetterInfoRequest request) {
    // Redis에서 CachedLetter 조회
    CachedLetter cachedLetter = cachedLetterRepository.findById(letterId)
        .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

    // letterKey 검증
    if (!cachedLetter.getLetterKey().equals(letterKey)) {
      throw new LetterException(LetterErrorStatus.NOT_LETTER_OWNER);
    }

    // 편지 정보 업데이트
    cachedLetter.setInfo(request.getTitle(), request.getSender());

    // 편지 내용 비교 후 단어 업데이트
    String oldContent = cachedLetter.getContent();
    String newContent = request.getContent();

    if (!oldContent.equals(newContent)) {
      List<CachedWord> updatedWords = updateWords(cachedLetter.getWords(), oldContent, newContent);
      cachedLetter.setWords(updatedWords);
      cachedLetter.setContent(newContent);
    }

    // Redis에 저장
    cachedLetterRepository.save(cachedLetter);
  }

  private List<CachedWord> updateWords(List<CachedWord> existingWords, String oldContent, String newContent) {
    List<String> oldWords = Arrays.asList(oldContent.trim().split("\\s+"));
    List<String> newWords = Arrays.asList(newContent.trim().split("\\s+"));

    List<CachedWord> updatedWords = new ArrayList<>();

    int oldIdx = 0;
    int newIdx = 0;

    while (newIdx < newWords.size()) {
      String newWord = newWords.get(newIdx);

      if (oldIdx < oldWords.size() && oldIdx < existingWords.size()) {
        String oldWord = oldWords.get(oldIdx);
        CachedWord existingWord = existingWords.get(oldIdx);

        if (oldWord.equals(newWord)) {
          // 동일한 단어 - 그대로 유지
          updatedWords.add(existingWord);
          oldIdx++;
          newIdx++;
        } else if (newWords.subList(newIdx, newWords.size()).contains(oldWord)) {
          // 새 단어가 추가된 경우 (기존 단어가 뒤에 있음)
          double prevOrder = updatedWords.isEmpty() ? 0 : updatedWords.get(updatedWords.size() - 1).getWordOrder();
          double nextOrder = existingWord.getWordOrder();
          double newOrder = (prevOrder + nextOrder) / 2;

          CachedWord addedWord = CachedWord.builder()
              .word(newWord)
              .startTime(null)
              .endTime(null)
              .wordOrder(newOrder)
              .build();
          updatedWords.add(addedWord);
          newIdx++;
        } else if (oldWords.subList(oldIdx, oldWords.size()).contains(newWord)) {
          // 기존 단어가 삭제된 경우 (새 단어가 뒤에 있음)
          oldIdx++;
        } else {
          // 단어가 변경된 경우
          CachedWord updatedWord = CachedWord.builder()
              .word(newWord)
              .startTime(existingWord.getStartTime())
              .endTime(existingWord.getEndTime())
              .wordOrder(existingWord.getWordOrder())
              .build();
          updatedWords.add(updatedWord);
          oldIdx++;
          newIdx++;
        }
      } else {
        // 기존 단어가 모두 처리되었고, 새 단어가 남은 경우 (끝에 추가)
        double prevOrder = updatedWords.isEmpty() ? 0 : updatedWords.get(updatedWords.size() - 1).getWordOrder();
        double newOrder = prevOrder + 1;

        CachedWord addedWord = CachedWord.builder()
            .word(newWord)
            .startTime(null)
            .endTime(null)
            .wordOrder(newOrder)
            .build();
        updatedWords.add(addedWord);
        newIdx++;
      }
    }

    // 남은 기존 단어들은 삭제된 것
    while (oldIdx < oldWords.size() && oldIdx < existingWords.size()) {
      CachedWord deletedWord = existingWords.get(oldIdx);
      oldIdx++;
    }

    return updatedWords;
  }
}
