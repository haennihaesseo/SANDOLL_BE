package haennihaesseo.sandoll.domain.letter.converter;

import haennihaesseo.sandoll.domain.deco.entity.Bgm;
import haennihaesseo.sandoll.domain.deco.entity.Template;
import haennihaesseo.sandoll.domain.font.entity.Font;
import haennihaesseo.sandoll.domain.letter.dto.response.LetterDetailResponse;
import haennihaesseo.sandoll.domain.letter.entity.Letter;
import haennihaesseo.sandoll.domain.letter.entity.Voice;
import haennihaesseo.sandoll.domain.letter.entity.Word;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LetterBoxConverter {

    public LetterDetailResponse toLetterDetailResponse(Letter letter, Bgm bgm, Template template, Font font, Voice voice, List<Word> words){
        return LetterDetailResponse.builder()
                .letterId(letter.getLetterId())
                .title(letter.getTitle())
                .sender(letter.getSenderName())
                .content(letter.getContent())
                .bgm(bgm != null ? toBgmInfo(bgm) : null)
                .template(template != null ? toTemplateInfo(template) : null)
                .font(font != null ? toFontInfo(font) : null)
                .voice(voice != null ? toVoiceInfo(voice) : null)
                .words(toWordInfos(words))
                .build();
    }

    private LetterDetailResponse.BgmInfo toBgmInfo(Bgm bgm) {
        return LetterDetailResponse.BgmInfo.builder()
                .bgmId(bgm.getBgmId())
                .bgmUrl(bgm.getBgmUrl())
                .build();
    }

    private LetterDetailResponse.TemplateInfo toTemplateInfo(Template template) {
        return LetterDetailResponse.TemplateInfo.builder()
                .templateId(template.getTemplateId())
                .templateUrl(template.getImageUrl())
                .build();
    }
    private LetterDetailResponse.FontInfo toFontInfo(Font font) {
        return LetterDetailResponse.FontInfo.builder()
                .fontId(font.getFontId())
                .fontUrl(font.getFontUrl())
                .build();
    }

    private LetterDetailResponse.VoiceInfo toVoiceInfo(Voice voice) {
        return LetterDetailResponse.VoiceInfo.builder()
                .voiceId(voice.getVoiceId())
                .voiceUrl(voice.getVoiceUrl())
                .build();
    }

    private List<LetterDetailResponse.WordInfo> toWordInfos(List<Word> words) {
        List<LetterDetailResponse.WordInfo> wordInfos = new ArrayList<>();

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

        return wordInfos;
    }
}
