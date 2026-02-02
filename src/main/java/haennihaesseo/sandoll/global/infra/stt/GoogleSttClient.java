package haennihaesseo.sandoll.global.infra.stt;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import haennihaesseo.sandoll.global.exception.GlobalException;
import haennihaesseo.sandoll.global.status.ErrorStatus;
import haennihaesseo.sandoll.global.util.ResourceLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class GoogleSttClient {

    private static final String LANGUAGE_CODE = "ko-KR";

    private final SpeechSettings speechSettings;

    public GoogleSttClient() {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    ResourceLoader.getResourceAsStream("google-stt-key.json"));
            this.speechSettings = SpeechSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Google Cloud 인증 파일 로드 실패", e);
        }
    }

    public SttResult transcribe(MultipartFile audioFile) {
        try {
            byte[] audioBytes = audioFile.getBytes();
            String contentType = audioFile.getContentType();
            RecognitionConfig.AudioEncoding encoding = getAudioEncoding(contentType);
            int channelCount = detectOpusChannelCount(audioBytes);

            log.info("[STT] contentType={}, encoding={}, channelCount={}", contentType, encoding, channelCount);

            return transcribeAudio(audioBytes, encoding, channelCount);
        } catch (IOException e) {
            log.error("오디오 파일 읽기 실패", e);
            throw new RuntimeException("오디오 파일 처리 중 오류가 발생했습니다.", e);
        }
    }

    private SttResult transcribeAudio(byte[] audioBytes, RecognitionConfig.AudioEncoding encoding, int channelCount) {
        try (SpeechClient speechClient = SpeechClient.create(speechSettings)) {
            RecognitionConfig config = buildConfig(encoding, channelCount);
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(ByteString.copyFrom(audioBytes))
                    .build();

            LongRunningRecognizeResponse response = speechClient
                    .longRunningRecognizeAsync(config, audio)
                    .get();

            log.info("Google STT 처리 완료");
            log.info("응답 결과: {}", response.getResultsList());

            return parseResponse(response);
        } catch (IOException e) {
            log.error("Google STT 처리 실패", e);
            throw new GlobalException(ErrorStatus.STT_SERVICE_ERROR);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Google STT 처리 중 인터럽트 발생", e);
            throw new GlobalException(ErrorStatus.STT_SERVICE_ERROR);
        } catch (ExecutionException e) {
            log.error("Google STT 비동기 처리 실패", e);
            throw new GlobalException(ErrorStatus.STT_SERVICE_ERROR);
        }
    }

    private RecognitionConfig buildConfig(RecognitionConfig.AudioEncoding encoding, int channelCount) {
        RecognitionConfig.Builder builder = RecognitionConfig.newBuilder()
                .setEncoding(encoding)
                .setLanguageCode(LANGUAGE_CODE)
                .setEnableWordTimeOffsets(true)
                .setEnableAutomaticPunctuation(true)
                .setModel("default");

        if (channelCount > 0) {
            builder.setAudioChannelCount(channelCount);
        }

        return builder.build();
    }

    /**
     * WEBM/OGG Opus 파일에서 OpusHead 헤더를 찾아 채널 수를 읽음
     * OpusHead 구조: [0-7] "OpusHead", [8] version, [9] channel count
     */
    private int detectOpusChannelCount(byte[] audioBytes) {
        byte[] magic = "OpusHead".getBytes();
        for (int i = 0; i <= audioBytes.length - magic.length - 2; i++) {
            boolean found = true;
            for (int j = 0; j < magic.length; j++) {
                if (audioBytes[i + j] != magic[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return audioBytes[i + 9] & 0xFF;
            }
        }
        return 0;
    }

    private SttResult parseResponse(LongRunningRecognizeResponse response) {
        if (response.getResultsList().isEmpty()) {
            return SttResult.builder()
                    .fullText("")
                    .totalDuration(0)
                    .words(Collections.emptyList())
                    .build();
        }

        List<SttWord> words = new ArrayList<>();
        StringBuilder fullText = new StringBuilder();
        int totalDuration = 0;
        double wordOrder = 0;

        for (SpeechRecognitionResult result : response.getResultsList()) {
            if (result.getAlternativesCount() == 0) continue;

            SpeechRecognitionAlternative alternative = result.getAlternatives(0);
            fullText.append(alternative.getTranscript()).append(" ");
            totalDuration = (int) toSeconds(result.getResultEndTime());

            for (WordInfo wordInfo : alternative.getWordsList()) {
                double startTime = toSeconds(wordInfo.getStartTime());
                double endTime = toSeconds(wordInfo.getEndTime());

                words.add(SttWord.builder()
                        .word(wordInfo.getWord())
                        .startTime(startTime)
                        .endTime(endTime)
                        .order(wordOrder++)
                        .build());
            }
        }

        return SttResult.builder()
                .fullText(fullText.toString().trim())
                .totalDuration(totalDuration)
                .words(words)
                .build();
    }

    private double toSeconds(com.google.protobuf.Duration duration) {
        return duration.getSeconds() + duration.getNanos() / 1_000_000_000.0;
    }

    private RecognitionConfig.AudioEncoding getAudioEncoding(String contentType) {
        if (contentType == null) {
            return RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED;
        }
        return switch (contentType.toLowerCase()) {
            case "audio/wav", "audio/wave", "audio/x-wav" -> RecognitionConfig.AudioEncoding.LINEAR16;
            case "audio/flac" -> RecognitionConfig.AudioEncoding.FLAC;
            case "audio/mp3", "audio/mpeg" -> RecognitionConfig.AudioEncoding.MP3;
            case "audio/ogg" -> RecognitionConfig.AudioEncoding.OGG_OPUS;
            case "audio/webm" -> RecognitionConfig.AudioEncoding.WEBM_OPUS;
            case "audio/amr" -> RecognitionConfig.AudioEncoding.AMR;
            default -> RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED;
        };
    }
}