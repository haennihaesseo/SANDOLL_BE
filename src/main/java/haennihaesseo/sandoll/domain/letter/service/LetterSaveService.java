package haennihaesseo.sandoll.domain.letter.service;

import haennihaesseo.sandoll.domain.deco.dto.response.BgmsResponse;
import haennihaesseo.sandoll.domain.deco.entity.Bgm;
import haennihaesseo.sandoll.domain.deco.entity.Template;
import haennihaesseo.sandoll.domain.deco.exception.DecoException;
import haennihaesseo.sandoll.domain.deco.repository.BgmRepository;
import haennihaesseo.sandoll.domain.deco.repository.TemplateRepository;
import haennihaesseo.sandoll.domain.deco.status.DecoErrorStatus;
import haennihaesseo.sandoll.domain.font.entity.Font;
import haennihaesseo.sandoll.domain.font.repository.FontRepository;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetter;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetterRepository;
import haennihaesseo.sandoll.domain.letter.cache.CachedWord;
import haennihaesseo.sandoll.domain.letter.dto.response.SecretLetterKeyResponse;
import haennihaesseo.sandoll.domain.letter.entity.Letter;
import haennihaesseo.sandoll.domain.letter.entity.LetterStatus;
import haennihaesseo.sandoll.domain.letter.entity.Voice;
import haennihaesseo.sandoll.domain.letter.entity.Word;
import haennihaesseo.sandoll.domain.letter.exception.LetterException;
import haennihaesseo.sandoll.domain.letter.repository.LetterRepository;
import haennihaesseo.sandoll.domain.letter.repository.VoiceRepository;
import haennihaesseo.sandoll.domain.letter.status.LetterErrorStatus;
import haennihaesseo.sandoll.domain.letter.util.AESUtil;
import haennihaesseo.sandoll.domain.user.entity.User;
import haennihaesseo.sandoll.domain.user.repository.UserRepository;
import haennihaesseo.sandoll.global.exception.GlobalException;
import haennihaesseo.sandoll.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


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
    private final AESUtil aesUtil;

    /**
     * 캐시의 편지 조회해서 저장 로직
     * @param userId
     * @param letterKey
     * @param password
     * @return
     */
    @Transactional
    // 저장해야되는거 voice, bgm, word
    public SecretLetterKeyResponse saveLetterAndLink(Long userId, String letterKey, String password){
        CachedLetter cachedLetter = cachedLetterRepository.findById(letterKey)
                .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.USER_NOT_FOUND));

        Template template = templateRepository.findById(cachedLetter.getTemplateId())
                .orElseThrow(() -> new DecoException(DecoErrorStatus.TEMPLATE_NOT_FOUND));

        Font font = fontRepository.findById(cachedLetter.getFontId())
                .orElseThrow(() -> new DecoException(DecoErrorStatus.FONT_NOT_FOUND));

        Voice voice = Voice.builder()
                .voiceUrl(cachedLetter.getVoiceUrl())
                .duration(cachedLetter.getDuration())
                .build();
        voiceRepository.save(voice);

        BgmsResponse.BgmDto bgmDto = cachedLetter.getBgmDto();
        Bgm bgm = null;
        if (bgmDto != null) {
            bgm = Bgm.builder()
                    .bgmId(cachedLetter.getBgmDto().bgmId())
                    .name(cachedLetter.getBgmDto().name())
                    .keyword(String.join(",", cachedLetter.getBgmDto().keyword()))
                    .build();
            bgmRepository.save(bgm);
        }

        Letter letter = Letter.builder()
                .senderName(cachedLetter.getSender())
                .title(cachedLetter.getTitle())
                .content(cachedLetter.getContent())
                .sender(user)
                .password(password != null ? password : "")
                .font(font)
                .template(template)
                .bgm(bgm)
                .voice(voice)
                .build();
        letterRepository.save(letter);

        try{
            String secretLetterKey = aesUtil.encrypt(letter.getLetterId());
            return new SecretLetterKeyResponse(secretLetterKey);
        } catch(Exception e){
            log.warn("공유키 암호화 중 예외 발생: letterId = {}", letter.getLetterId(), e);
            throw new LetterException(LetterErrorStatus.LETTER_ENCRYPT_FAILED);
        }
    }

}
