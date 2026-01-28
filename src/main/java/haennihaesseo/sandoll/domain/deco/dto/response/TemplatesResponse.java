package haennihaesseo.sandoll.domain.deco.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TemplatesResponse {

    private List<TemplateDto> templates;

    @Builder
    public record TemplateDto(Long templateId, String name, String previewImageUrl){};
}
