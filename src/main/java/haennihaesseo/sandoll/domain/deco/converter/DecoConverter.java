package haennihaesseo.sandoll.domain.deco.converter;

import haennihaesseo.sandoll.domain.deco.dto.response.BgmsResponse;
import haennihaesseo.sandoll.domain.deco.dto.response.TemplatesResponse;
import haennihaesseo.sandoll.domain.deco.entity.Bgm;
import haennihaesseo.sandoll.domain.deco.entity.Template;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DecoConverter {

    public TemplatesResponse toTemplatesResponse(List<Template> templates) {
        List<TemplatesResponse.TemplateDto> temps = templates.stream()
                .map(t -> TemplatesResponse.TemplateDto.builder()
                            .templateId(t.getTemplateId())
                            .name(t.getName())
                            .previewImageUrl(t.getPreviewImageUrl())
                            .build())
                .toList();
        return TemplatesResponse.builder()
                .templates(temps)
                .build();
    }

    public List<BgmsResponse.BgmDto> toBgmDto(List<Bgm> bgms) {
        return bgms.stream()
                .map(b -> BgmsResponse.BgmDto.builder()
                        .bgmId(b.getBgmId())
                        .bgmUrl(b.getBgmUrl())
                        .keyword(Arrays.stream(b.getKeyword().split(",")).toList())
                        .name(b.getName())
                        .build())
                .toList();
    }

    private BgmsResponse toBgmsResponse(List<Bgm> bgms) {
        return BgmsResponse.builder()
                .bgms(toBgmDto(bgms))
                .build();
    }
}
