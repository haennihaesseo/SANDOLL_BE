package haennihaesseo.sandoll.domain.deco.converter;

import haennihaesseo.sandoll.domain.deco.dto.response.TemplatesResponse;
import haennihaesseo.sandoll.domain.deco.entity.Template;
import org.springframework.stereotype.Component;

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
}
