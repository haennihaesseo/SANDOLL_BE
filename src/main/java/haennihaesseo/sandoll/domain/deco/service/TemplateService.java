package haennihaesseo.sandoll.domain.deco.service;

import haennihaesseo.sandoll.domain.deco.converter.DecoConverter;
import haennihaesseo.sandoll.domain.deco.dto.response.TemplatesResponse;
import haennihaesseo.sandoll.domain.deco.entity.Template;
import haennihaesseo.sandoll.domain.deco.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final DecoConverter decoConverter;

    /**
     * DB의 전체 템플릿 조회
     * @return templates(templateId, name, previewImageUrl)
     */
    public TemplatesResponse getAllTemplates() {
        List<Template> templates = templateRepository.findAll();
        return decoConverter.toTemplatesResponse(templates);
    }
}
