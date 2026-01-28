package haennihaesseo.sandoll.domain.deco.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BgmsResponse {

    public List<BgmDto> bgms;

    @Builder
    public record BgmDto(Long bgmId, String bgmUrl, List<String> keyword, String name) {};
}
