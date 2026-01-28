package haennihaesseo.sandoll.domain.letter.service;

import haennihaesseo.sandoll.domain.letter.cache.CachedLetter;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetterRepository;
import haennihaesseo.sandoll.domain.letter.cache.CachedWord;
import haennihaesseo.sandoll.domain.letter.dto.response.VoiceSaveResponse;
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

}
