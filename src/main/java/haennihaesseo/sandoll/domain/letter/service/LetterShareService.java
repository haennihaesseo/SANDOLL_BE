package haennihaesseo.sandoll.domain.letter.service;

import haennihaesseo.sandoll.domain.letter.dto.response.LetterDetailResponse;
import haennihaesseo.sandoll.domain.letter.dto.response.SecretLetterKeyResponse;
import haennihaesseo.sandoll.domain.letter.entity.Letter;
import haennihaesseo.sandoll.domain.letter.entity.ReceiverLetter;
import haennihaesseo.sandoll.domain.letter.entity.ReceiverLetterId;
import haennihaesseo.sandoll.domain.letter.exception.LetterException;
import haennihaesseo.sandoll.domain.letter.repository.LetterRepository;
import haennihaesseo.sandoll.domain.letter.repository.ReceiverLetterRepository;
import haennihaesseo.sandoll.domain.letter.status.LetterErrorStatus;
import haennihaesseo.sandoll.domain.letter.util.AESUtil;
import haennihaesseo.sandoll.domain.user.entity.User;
import haennihaesseo.sandoll.domain.user.repository.UserRepository;
import haennihaesseo.sandoll.global.exception.GlobalException;
import haennihaesseo.sandoll.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class LetterShareService {

    private final LetterDetailService letterDetailService;
    private final AESUtil aesUtil;
    private final LetterRepository letterRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ReceiverLetterRepository receiverLetterRepository;

    /**
     * 편지 아이디를 통해 암호화된 편지아이디를 발급
     * @param letterId
     * @return
     */
    public SecretLetterKeyResponse getLetterSecretKeyByLetterId(Long userId, Long letterId) {
        if (!receiverLetterRepository.existsByIdReceiverIdAndIdLetterId(userId, letterId))
            throw new LetterException(LetterErrorStatus.NOT_OWN_LETTER);
        try{
            String secretLetterKey = aesUtil.encrypt(letterId);
            return new SecretLetterKeyResponse(secretLetterKey);
        } catch(Exception e){
            log.warn("공유키 암호화 중 예외 발생: letterId = {}", letterId, e);
            throw new LetterException(LetterErrorStatus.LETTER_ENCRYPT_FAILED);
        }
    }

    /**
     * 암호화된 편지 아이디와 비밀번호를 이용해 링크로 편지 조회
     * @param secretLetterKey
     * @param password
     * @return
     */
    public LetterDetailResponse getLetterDetailsByLink(String secretLetterKey, String password) {
        Long letterId = aesUtil.decrypt(secretLetterKey);
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

        if (letter.getPassword() != null) {
            if (password == null)
                throw new GlobalException(LetterErrorStatus.LETTER_NEED_PASSWORD);
            if (!passwordEncoder.matches(password, letter.getPassword()))
                throw new GlobalException(LetterErrorStatus.LETTER_WRONG_PASSWORD);
        }
        return letterDetailService.getLetterDetails(letterId);
    }

    /**
     * 링크로 공유받은 편지 받은 편지함에 저장
     * @param userId
     * @param letterId
     */
    public void saveLetterInMyBox(Long userId, Long letterId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.USER_NOT_FOUND));

        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

        // 자기가 작성자인경우
        if (Objects.equals(letter.getSender().getUserId(), user.getUserId())) {
            throw new LetterException(LetterErrorStatus.CANNOT_SAVE_OWN_LETTER);
        }

        // 이미 보관함에 저장한 경우
        if (receiverLetterRepository.existsByIdReceiverIdAndIdLetterId(userId, letterId))
            throw new LetterException(LetterErrorStatus.ALREADY_SAVE_LETTER);

        // 보관함에 저장
        ReceiverLetterId receiverLetterId = new ReceiverLetterId(userId, letterId);
        ReceiverLetter receiverLetter = ReceiverLetter.builder()
                .id(receiverLetterId)
                .user(user)
                .letter(letter)
                .build();
        receiverLetterRepository.save(receiverLetter);
    }
}
