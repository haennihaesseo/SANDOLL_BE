package haennihaesseo.sandoll.domain.letter.cache;

import haennihaesseo.sandoll.domain.deco.dto.response.BgmsResponse;
import haennihaesseo.sandoll.domain.font.entity.Font;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "letter")
public class CachedLetter implements Serializable {

    @Id
    private String letterId;

    private String letterKey;

    private String voiceUrl;
    private Integer duration;
    private String content;

    private String title;
    private String sender;

    private List<Long> contextFontIds;
    private List<String> contextFontKeywords;
    private List<Long> voiceFontIds;
    private List<String> voiceFontKeywords;

    @Builder.Default
    private List<Long> shownContextFontIds = new ArrayList<>();

    @Builder.Default
    private List<Long> shownVoiceFontIds = new ArrayList<>();

    private Long fontId;
    private String fontUrl;

    private Long templateId;
    private String templateUrl;
    private BgmsResponse.BgmDto bgmDto;

    @Builder.Default
    private List<CachedWord> words = new ArrayList<>();

    @TimeToLive
    @Builder.Default
    private Long ttl = 3600L;  // 1시간 (초 단위)

    public void setInfo(String title, String sender) {
        this.title = title;
        this.sender = sender;
    }

    public void setWords(List<CachedWord> words) {
        this.words = words;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public void setBgmDto(BgmsResponse.BgmDto bgmDto) {
        this.bgmDto = bgmDto;
    }


    public void setFont(Long fontId, String fontUrl) {
        this.fontId = fontId;
        this.fontUrl = fontUrl;
    }

    public void setVoiceFonts(List<Font> fonts, List<String> keywords) {
        List<String> fontIds = new ArrayList<>();
        for (Font font : fonts) {
            fontIds.add(font.getFontId().toString());
        }
        this.voiceFontIds = fonts.stream().map(Font::getFontId).toList();
        this.voiceFontKeywords = keywords;
    }

    public void setTemplateUrl(String templateUrl) {
        this.templateUrl = templateUrl;
    }

    public void setShownContextFontIds(List<Long> ids) {
        this.shownContextFontIds = ids;
    }
}