package haennihaesseo.sandoll.domain.deco.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BgmsResponse {

    private List<BgmDto> bgms;

    @Builder
    public record BgmDto(Long bgmId, String bgmUrl, List<String> keyword, String name, Double bgmSize) {
        public BgmDto withBgmSize(Double bgmSize) {
            return new BgmDto(bgmId, bgmUrl, keyword, name, bgmSize);
        }
    };
}
