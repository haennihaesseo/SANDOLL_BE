package haennihaesseo.sandoll.domain.letter.cache;

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

    private String contextKeywords;
    private String recommendedVoiceFonts;

    private Long fontId;

    @Builder.Default
    private List<CachedWord> words = new ArrayList<>();

    @TimeToLive
    @Builder.Default
    private Long ttl = 3600L;  // 1시간 (초 단위)

    public void addWord(CachedWord word) {
        this.words.add(word);
    }

    public void addWords(List<CachedWord> words) {
        this.words.addAll(words);
    }

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

    public void setFontId(Long fontId) {
        this.fontId = fontId;
    }
}