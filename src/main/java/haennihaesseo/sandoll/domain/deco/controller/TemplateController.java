package haennihaesseo.sandoll.domain.deco.controller;

import haennihaesseo.sandoll.domain.deco.dto.response.TemplateImageResponse;
import haennihaesseo.sandoll.domain.deco.dto.response.TemplatesResponse;
import haennihaesseo.sandoll.domain.deco.service.TemplateService;
import haennihaesseo.sandoll.domain.deco.status.DecoSuccessStatus;
import haennihaesseo.sandoll.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Template", description = "Template 조회 및 적용 API")
@RestController
@RequestMapping("/api/deco/template")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @Operation(
            summary = "[4.1] 편지지 전체 조회"
    )
    @GetMapping
    ResponseEntity<ApiResponse<TemplatesResponse>> getTemplates(){
        TemplatesResponse responses = templateService.getAllTemplates();
        return ApiResponse.success(DecoSuccessStatus.SUCCESS_401, responses);
    }

    @Operation(
            summary = "[4.2] 편지지 편지에 적용"
    )
    @PostMapping
    ResponseEntity<ApiResponse<TemplateImageResponse>> setTemplate(
        @RequestHeader(name = "letterId") String letterId,
        @RequestParam(name = "templateId") Long templateId
    ){
        TemplateImageResponse response = templateService.setTemplateToLetter(letterId, templateId);
        return ApiResponse.success(DecoSuccessStatus.SUCCESS_402, response);
    }
}
