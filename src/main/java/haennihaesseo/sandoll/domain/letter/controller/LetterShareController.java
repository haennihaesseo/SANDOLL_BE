package haennihaesseo.sandoll.domain.letter.controller;

import haennihaesseo.sandoll.domain.letter.dto.request.LetterLinkViewRequest;
import haennihaesseo.sandoll.domain.letter.dto.response.LetterDetailResponse;
import haennihaesseo.sandoll.domain.letter.dto.response.SecretLetterKeyResponse;
import haennihaesseo.sandoll.domain.letter.service.LetterShareService;
import haennihaesseo.sandoll.domain.letter.status.LetterSuccessStatus;
import haennihaesseo.sandoll.global.auth.principal.UserPrincipal;
import haennihaesseo.sandoll.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "LetterShare", description = "편지 저장 및 공유 조회 API")
@RestController
@RequestMapping("/api/letter")
@RequiredArgsConstructor
@Slf4j
public class LetterShareController {

    private final LetterShareService letterShareService;

    @Operation(
            summary = "[2.5] 편지 링크를 위한 암호화된 편지 아이디 조회"
    )
    @GetMapping("/{letterId}/share")
    public ResponseEntity<ApiResponse<SecretLetterKeyResponse>> getSecretLetterKey(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "편지의 고유키", required = true)
            @PathVariable(name = "letterId") Long letterId
    ) {
        Long userId = userPrincipal.getUser().getUserId();
        SecretLetterKeyResponse response = letterShareService.getLetterSecretKeyByLetterId(userId, letterId);
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_205, response);
    }

    @Operation(
            summary = "[6.1] 링크를 통해 편지 조회"
    )
    @PostMapping("/view")
    public ResponseEntity<ApiResponse<LetterDetailResponse>> getLetterDetailByLink(
            @RequestBody @Valid LetterLinkViewRequest letterLinkViewRequest
    ){
        LetterDetailResponse response = letterShareService.getLetterDetailsByLink(letterLinkViewRequest.getSecretLetterId(), letterLinkViewRequest.getPassword());
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_601, response);
    }

    @Operation(
            summary = "[6.2] 편지 보관함에 저장"
    )
    @PostMapping("/{letterId}/save")
    public ResponseEntity<ApiResponse<Void>> saveLetter(
            @PathVariable(name = "letterId") Long letterId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        Long userId = userPrincipal.getUser().getUserId();
        letterShareService.saveLetterInMyBox(userId, letterId);
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_602);
    }
}
