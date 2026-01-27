package haennihaesseo.sandoll.domain.letter.service;

import haennihaesseo.sandoll.domain.letter.dto.LetterDetailResponse;
import haennihaesseo.sandoll.domain.letter.dto.LetterType;
import haennihaesseo.sandoll.domain.letter.dto.ReceiveLetterResponse;
import haennihaesseo.sandoll.domain.letter.dto.OrderStatus;
import haennihaesseo.sandoll.domain.letter.entity.Letter;
import haennihaesseo.sandoll.domain.letter.entity.LetterStatus;
import haennihaesseo.sandoll.domain.letter.entity.ReceiverLetterId;
import haennihaesseo.sandoll.domain.letter.entity.Word;
import haennihaesseo.sandoll.domain.letter.repository.WordRepository;
import haennihaesseo.sandoll.domain.letter.status.LetterErrorStatus;
import haennihaesseo.sandoll.domain.letter.exception.LetterException;
import haennihaesseo.sandoll.domain.letter.repository.LetterRepository;
import haennihaesseo.sandoll.domain.letter.repository.ReceiverLetterRepository;
import haennihaesseo.sandoll.domain.user.entity.User;
import haennihaesseo.sandoll.domain.user.repository.UserRepository;
import haennihaesseo.sandoll.global.exception.GlobalException;
import haennihaesseo.sandoll.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LetterService {

    private final LetterRepository letterRepository;
    private final ReceiverLetterRepository receiverLetterRepository;
    private final UserRepository userRepository;
    private final WordRepository wordRepository;

    public List<ReceiveLetterResponse> getReceivedLettersByUser(Long userId, OrderStatus status) {

        userRepository.findById(userId).orElseThrow(() -> new GlobalException(ErrorStatus.USER_NOT_FOUND));

        List<Long> letterIds = (status.equals(OrderStatus.EARLIEST))
                ? receiverLetterRepository.findIdLetterIdByIdReceiverIdOrderByCreatedAtDesc(userId)
                : receiverLetterRepository.findIdLetterIdByIdReceiverIdOrderByCreatedAtAsc(userId);

        List<ReceiveLetterResponse> results = new ArrayList<>();

        for (Long letterId : letterIds) {
            Letter letter = letterRepository.findById(letterId)
                    .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

            results.add(
                    ReceiveLetterResponse.builder()
                            .letterId(letterId)
                            .sender(letter.getSenderName())
                            .createdAt(letter.getCreatedAt().toLocalDate())
                            .build()
            );
        }
        return results;
    }

    public LetterDetailResponse getLetterDetailsByLetter(Long letterId) {

        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

        log.info("단어 조회 시작");
        List<Word> words = wordRepository.findByLetterLetterIdOrderByWordOrderAsc(letterId);
        List<LetterDetailResponse.WordInfo> wordInfos = new ArrayList<>();

        log.info("word를 wordInfo 리스트로 변환시작");
        for (Word word : words) {
            wordInfos.add(
                    LetterDetailResponse.WordInfo.builder()
                            .wordId(word.getWordId())
                            .word(word.getWord())
                            .startTime(word.getStartTime())
                            .endTime(word.getEndTime())
                            .build()
            );
        }

        log.info("최종 응답 생성");
        LetterDetailResponse result = LetterDetailResponse.builder()
                .letterId(letterId)
                .title(letter.getTitle())
                .sender(letter.getSenderName())
                .content(letter.getContent())
                .bgm(
                        letter.getBgm() != null ?
                            LetterDetailResponse.BgmInfo.builder()
                                    .bgmId(letter.getBgm().getBgmId())
                                    .bgmUrl(letter.getBgm().getBgmUrl())
                                    .build()
                                : null
                )
                .template(
                        letter.getTemplate() != null ?
                            LetterDetailResponse.TemplateInfo.builder()
                                    .templateId(letter.getTemplate().getTemplateId())
                                    .templateUrl(letter.getTemplate().getImageUrl())
                                    .build()
                                : null
                )
                .font(
                        letter.getDefaultFont() != null ?
                            LetterDetailResponse.FontInfo.builder()
                                    .fontId(letter.getDefaultFont().getFontId())
                                    .fontUrl(letter.getDefaultFont().getFontUrl())
                                    .build()
                                : null
                )
                .voice(
                        letter.getDefaultFont() != null ?
                            LetterDetailResponse.VoiceInfo.builder()
                                    .voiceId(letter.getVoice().getVoiceId())
                                    .voiceUrl(letter.getVoice().getVoiceUrl())
                                    .build()
                                : null
                )
                .words(wordInfos)
                .build();

        return  result;
    }

    public void hideLetter(Long userId, LetterType letterType, List<Long> letterIds) {

        userRepository.findById(userId).orElseThrow(() -> new GlobalException(ErrorStatus.USER_NOT_FOUND));

        if (letterType.equals(LetterType.RECEIVE)){
            for (Long letterId : letterIds) {
                ReceiverLetterId id = new ReceiverLetterId(userId, letterId);
                if (!receiverLetterRepository.existsById(id))
                    throw new LetterException(LetterErrorStatus.NOT_LETTER_OWNER);
                receiverLetterRepository.deleteById(id);
            }
        } else {
            for (Long letterId : letterIds) {
                Letter letter = letterRepository.findByLetterIdAndSenderUserId(letterId, userId);
                if (letter == null)
                    throw new LetterException(LetterErrorStatus.NOT_LETTER_OWNER);
                letter.setLetterStatus(LetterStatus.INVISIBLE);
                letterRepository.save(letter);
            }
        }
    }
}
