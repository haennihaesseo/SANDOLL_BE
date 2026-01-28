package haennihaesseo.sandoll.domain.deco.controller;

import haennihaesseo.sandoll.domain.deco.dto.response.TemplatesResponse;
import haennihaesseo.sandoll.domain.deco.service.TemplateService;
import haennihaesseo.sandoll.domain.deco.status.DecoSuccessStatus;
import haennihaesseo.sandoll.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deco")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping("/template")
    ResponseEntity<ApiResponse<TemplatesResponse>> getTemplates(){
        TemplatesResponse responses = templateService.getAllTemplates();
        return ApiResponse.success(DecoSuccessStatus.SUCCESS_401, responses);
    }
}
