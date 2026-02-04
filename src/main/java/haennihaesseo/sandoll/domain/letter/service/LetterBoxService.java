package haennihaesseo.sandoll.domain.letter.service;

import haennihaesseo.sandoll.domain.letter.converter.LetterBoxConverter;
import haennihaesseo.sandoll.domain.letter.dto.request.LetterType;
import haennihaesseo.sandoll.domain.letter.dto.request.OrderStatus;
import haennihaesseo.sandoll.domain.letter.dto.response.*;
import haennihaesseo.sandoll.domain.letter.entity.Letter;
import haennihaesseo.sandoll.domain.letter.entity.LetterStatus;
import haennihaesseo.sandoll.domain.letter.entity.ReceiverLetterId;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LetterBoxService {

    private final LetterRepository letterRepository;
    private final ReceiverLetterRepository receiverLetterRepository;
    private final UserRepository userRepository;
    private final LetterDetailService letterDetailService;

    public List<ReceiveLetterResponse> getReceivedLettersByUser(Long userId, OrderStatus status) {
        userRepository.findById(userId).orElseThrow(() -> new GlobalException(ErrorStatus.USER_NOT_FOUND));

        List<Letter> letters;
        if (status.equals(OrderStatus.LATEST))
            letters = receiverLetterRepository.findReceivedLettersByUserIdOrderByCreatedAtDesc(userId);
        else
            letters = receiverLetterRepository.findReceivedLettersByUserIdOrderByCreatedAtAsc(userId);

        return letters.stream()
                .map(l -> ReceiveLetterResponse.builder()
                        .letterId(l.getLetterId())
                        .sender(l.getSenderName())
                        .createdAt(l.getCreatedAt().toLocalDate())
                        .build()
                )
                .toList();
    }

    public LetterDetailResponse getLetterDetailsByLetter(Long userId, Long letterId) {
        if (!letterRepository.existsByLetterIdAndSenderUserId(letterId, userId)
                && !receiverLetterRepository.existsByIdReceiverIdAndIdLetterId(userId, letterId))
            throw new LetterException(LetterErrorStatus.NOT_OWN_LETTER);
        return letterDetailService.getLetterDetails(letterId);
    }

    @Transactional
    public void hideLetter(Long userId, LetterType letterType, List<Long> letterIds) {
        userRepository.findById(userId).orElseThrow(() -> new GlobalException(ErrorStatus.USER_NOT_FOUND));

        if (letterType.equals(LetterType.RECEIVE)){
            int deletedCount = receiverLetterRepository.deleteAllByIdReceiverIdAndIdLetterIdIn(userId, letterIds);
            if (deletedCount != letterIds.size())
                throw new LetterException(LetterErrorStatus.NOT_LETTER_OWNER);
        } else {
            int updatedCount = letterRepository.updateLetterStatusBySenderUserIdAndLetterIdIn(LetterStatus.INVISIBLE, userId, letterIds);
            if (updatedCount != letterIds.size())
                throw new LetterException(LetterErrorStatus.NOT_LETTER_OWNER);
        }
    }

    public List<SendLetterResponse> getSentLettersByUser(Long userId, OrderStatus status) {
        userRepository.findById(userId).orElseThrow(() -> new GlobalException(ErrorStatus.USER_NOT_FOUND));

        List<Letter> letters;
        if (status.equals(OrderStatus.LATEST))
            letters = letterRepository.findBySenderUserIdAndLetterStatusOrderByCreatedAtDesc(userId, LetterStatus.VISIBLE);
        else
            letters = letterRepository.findBySenderUserIdAndLetterStatusOrderByCreatedAtAsc(userId, LetterStatus.VISIBLE);

        return letters.stream()
                .map(l -> SendLetterResponse.builder()
                        .letterId(l.getLetterId())
                        .title(l.getTitle())
                        .createdAt(l.getCreatedAt().toLocalDate())
                        .build()
                )
                .toList();
    }

    public HomeResponse getHomeLetterCount(Long userId) {
        Long count;
        if(userId == null) {
            // 생성된 전체 편지 개수 조회
            count = letterRepository.count();
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new GlobalException(ErrorStatus.USER_NOT_FOUND));

            count = receiverLetterRepository.countByIdReceiverId(userId);
        }

        return HomeResponse.builder()
            .count(count)
            .build();
    }
}
