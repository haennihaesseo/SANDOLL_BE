package haennihaesseo.sandoll.domain.deco.service;

import haennihaesseo.sandoll.domain.deco.converter.DecoConverter;
import haennihaesseo.sandoll.domain.deco.dto.response.TemplateImageResponse;
import haennihaesseo.sandoll.domain.deco.dto.response.TemplatesResponse;
import haennihaesseo.sandoll.domain.deco.entity.Template;
import haennihaesseo.sandoll.domain.deco.entity.enums.Size;
import haennihaesseo.sandoll.domain.deco.exception.DecoException;
import haennihaesseo.sandoll.domain.deco.repository.TemplateRepository;
import haennihaesseo.sandoll.domain.deco.status.DecoErrorStatus;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetter;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetterRepository;
import haennihaesseo.sandoll.domain.letter.exception.LetterException;
import haennihaesseo.sandoll.domain.letter.status.LetterErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final DecoConverter decoConverter;
    private final CachedLetterRepository cachedLetterRepository;

    /**
     * DB의 전체 템플릿 조회
     * @return templates(templateId, name, previewImageUrl)
     */
    public TemplatesResponse getAllTemplates() {
        List<Template> templates = templateRepository.findAll();
        return decoConverter.toTemplatesResponse(templates);
    }

    /**
     * preview 이미지 선택한 템플릿 내용 길이에 맞게 적용
     * @param letterId
     * @param templateId
     * @return
     */
    public TemplateImageResponse setTemplateToLetter(String letterId, Long templateId) {

        CachedLetter cachedLetter = cachedLetterRepository.findById(letterId)
                .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new DecoException(DecoErrorStatus.TEMPLATE_NOT_FOUND));

        int length = (cachedLetter.getContent() != null) ? cachedLetter.getContent().length() : 0;
        Size size = Size.fromLength(length);

        Template setTemplate = templateRepository.findByNameAndSize(template.getName(), size);

        if (setTemplate == null)
            throw new DecoException(DecoErrorStatus.TEMPLATE_NOT_FOUND);

        cachedLetter.setTemplateId(setTemplate.getTemplateId());
        cachedLetter.setTemplateUrl(setTemplate.getImageUrl());
        cachedLetterRepository.save(cachedLetter);

        return new TemplateImageResponse(setTemplate.getImageUrl());
    }
}
