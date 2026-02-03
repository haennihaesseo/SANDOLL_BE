package haennihaesseo.sandoll.domain.letter.service;

import haennihaesseo.sandoll.domain.deco.dto.response.BgmsResponse;
import haennihaesseo.sandoll.domain.deco.entity.Bgm;
import haennihaesseo.sandoll.domain.deco.entity.Template;
import haennihaesseo.sandoll.domain.deco.exception.DecoException;
import haennihaesseo.sandoll.domain.deco.repository.BgmRepository;
import haennihaesseo.sandoll.domain.deco.repository.TemplateRepository;
import haennihaesseo.sandoll.domain.deco.status.DecoErrorStatus;
import haennihaesseo.sandoll.domain.font.entity.Font;
import haennihaesseo.sandoll.domain.font.exception.FontException;
import haennihaesseo.sandoll.domain.font.repository.FontRepository;
import haennihaesseo.sandoll.domain.font.status.FontErrorStatus;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetter;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetterRepository;
import haennihaesseo.sandoll.domain.letter.cache.CachedWord;
import haennihaesseo.sandoll.domain.letter.dto.response.LetterDetailResponse;
import haennihaesseo.sandoll.domain.letter.dto.response.SecretLetterKeyResponse;
import haennihaesseo.sandoll.domain.letter.entity.Letter;
import haennihaesseo.sandoll.domain.letter.entity.LetterStatus;
import haennihaesseo.sandoll.domain.letter.entity.Voice;
import haennihaesseo.sandoll.domain.letter.entity.Word;
import haennihaesseo.sandoll.domain.letter.exception.LetterException;
import haennihaesseo.sandoll.domain.letter.repository.LetterRepository;
import haennihaesseo.sandoll.domain.letter.repository.VoiceRepository;
import haennihaesseo.sandoll.domain.letter.repository.WordRepository;
import haennihaesseo.sandoll.domain.letter.status.LetterErrorStatus;
import haennihaesseo.sandoll.domain.letter.util.AESUtil;
import haennihaesseo.sandoll.domain.user.entity.User;
import haennihaesseo.sandoll.domain.user.repository.UserRepository;
import haennihaesseo.sandoll.global.exception.GlobalException;
import haennihaesseo.sandoll.global.infra.RedisClient;
import haennihaesseo.sandoll.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Slf4j
public class LetterSaveService {

    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final CachedLetterRepository cachedLetterRepository;
    private final FontRepository fontRepository;
    private final VoiceRepository voiceRepository;
    private final LetterRepository letterRepository;
    private final BgmRepository bgmRepository;
    private final WordRepository wordRepository;
    private final AESUtil aesUtil;
    private final PasswordEncoder passwordEncoder;
    private final LetterDetailService letterDetailService;
    private final RedisClient redisClient;

    /**
     * 캐시의 편지 조회해서 저장 로직
     * @param userId
     * @param letterId
     * @return
     */
    @Transactional
    public SecretLetterKeyResponse saveLetterAndLink(Long userId, String letterId){
        CachedLetter cachedLetter = cachedLetterRepository.findById(letterId)
                .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.USER_NOT_FOUND));

        Template template = templateRepository.findById(cachedLetter.getTemplateId())
                .orElseThrow(() -> new DecoException(DecoErrorStatus.TEMPLATE_NOT_FOUND));

        Font font = fontRepository.findById(cachedLetter.getFontId())
                .orElseThrow(() -> new FontException(FontErrorStatus.FONT_NOT_FOUND));

        // 목소리 저장
        Voice voice = Voice.builder()
                .voiceUrl(cachedLetter.getVoiceUrl())
                .duration(cachedLetter.getDuration())
                .build();
        voiceRepository.save(voice);

        // 브금 저장
        BgmsResponse.BgmDto bgmDto = cachedLetter.getBgmDto();
        Bgm bgm = null;
        if (bgmDto != null) {
            bgm = Bgm.builder()
                    .name(cachedLetter.getBgmDto().name())
                    .keyword(String.join(",", cachedLetter.getBgmDto().keyword()))
                    .bgmUrl(cachedLetter.getBgmDto().bgmUrl())
                    .bgmSize(cachedLetter.getBgmDto().bgmSize())
                    .build();
            bgmRepository.save(bgm);
        }

        // 편지 저장
        Letter letter = Letter.builder()
                .senderName(cachedLetter.getSender())
                .title(cachedLetter.getTitle())
                .content(cachedLetter.getContent())
                .sender(user)
                .font(font)
                .template(template)
                .bgm(bgm)
                .voice(voice)
                .build();
        letterRepository.save(letter);

        // todo 성능 위해 추후 리팩토링 예정
        // word 저장
        cachedLetter.getWords().stream()
                .sorted(Comparator.comparing(CachedWord::getWordOrder))
                .map(w -> Word.builder()
                        .word(w.getWord())
                        .startTime(w.getStartTime())
                        .endTime(w.getEndTime())
                        .letter(letter)
                        .build())
                .forEach(wordRepository::save);

        try{
            String secretLetterKey = aesUtil.encrypt(letter.getLetterId());
            // 캐시에서 bgm 데이터 삭제
            redisClient.deleteData("bgms", letterId);
            // 캐시에서 letterId 제거
            cachedLetterRepository.deleteById(letterId);
            return new SecretLetterKeyResponse(secretLetterKey);
        } catch(Exception e){
            log.warn("공유키 암호화 중 예외 발생: letterId = {}", letter.getLetterId(), e);
            throw new LetterException(LetterErrorStatus.LETTER_ENCRYPT_FAILED);
        }
    }

    /**
     * 저장된 편지에 비밀번호 설정
     * @param userId
     * @param secretLetterKey
     * @param password
     */
    @Transactional
    public void updateLetterPasswordBySecretLetterKey(Long userId, String secretLetterKey, String password) {
        Letter letter = decryptLetter(userId, secretLetterKey);
        letter.setPassword(passwordEncoder.encode(password));
        letterRepository.save(letter);
    }

    public LetterDetailResponse viewLetterBehindShare(Long userId, String secretLetterKey) {
        Long letterId = decryptLetter(userId, secretLetterKey).getLetterId();
        return letterDetailService.getLetterDetails(letterId);
    }

    private Letter decryptLetter(Long userId, String secretLetterKey) {
        Long letterId;
        try {
            letterId = aesUtil.decrypt(secretLetterKey);
        } catch (Exception e){
            log.warn("공유키 복호화 중 예외 발생: secretLetterKey = {}", secretLetterKey, e);
            throw new LetterException(LetterErrorStatus.LETTER_DECRYPT_FAILED);
        }

        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

        // 보낸 작성자가 아닌 경우
        if (!letter.getSender().getUserId().equals(userId))
            throw new LetterException(LetterErrorStatus.NOT_LETTER_OWNER);

        return letter;
    }
}
