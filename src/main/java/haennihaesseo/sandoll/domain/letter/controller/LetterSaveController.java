package haennihaesseo.sandoll.domain.letter.controller;

import haennihaesseo.sandoll.domain.letter.dto.request.LetterLinkViewRequest;
import haennihaesseo.sandoll.domain.letter.dto.request.LetterPasswordRequest;
import haennihaesseo.sandoll.domain.letter.dto.response.LetterDetailResponse;
import haennihaesseo.sandoll.domain.letter.dto.response.SecretLetterKeyResponse;
import haennihaesseo.sandoll.domain.letter.service.LetterDetailService;
import haennihaesseo.sandoll.domain.letter.service.LetterSaveService;
import haennihaesseo.sandoll.domain.letter.status.LetterSuccessStatus;
import haennihaesseo.sandoll.global.auth.principal.UserPrincipal;
import haennihaesseo.sandoll.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Letter Save", description = "편지 저장 API")
@RestController
@RequestMapping("/api/letter")
@RequiredArgsConstructor
public class LetterSaveController {

    private final LetterSaveService letterSaveService;

    @Operation(
            summary = "[5.1] 작성한 편지 저장 및 암호화된 편지 아이디 리턴"
    )
    @PostMapping("/share")
    public ResponseEntity<ApiResponse<SecretLetterKeyResponse>> saveLetter(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestHeader("letterId") String letterId
    ){
        Long userId = userPrincipal.getUser().getUserId();
        SecretLetterKeyResponse response = letterSaveService.saveLetterAndLink(userId, letterId);
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_501, response);
    }

    @Operation(
            summary = "[5.2] 작성한 편지에 비밀번호 설정"
    )
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody LetterPasswordRequest letterPasswordRequest
    ){
        Long userId = userPrincipal.getUser().getUserId();
        letterSaveService.updateLetterPasswordBySecretLetterKey(userId, letterPasswordRequest.getSecretLetterKey(), letterPasswordRequest.getPassword());
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_502);
    }

    @Operation(
            summary = "[5.3] 작성한 편지 조회"
    )
    @GetMapping("/share")
    public ResponseEntity<ApiResponse<LetterDetailResponse>> viewShareLetter(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "secretLetterKey") String secretLetterKey
    ){
        Long userId = userPrincipal.getUser().getUserId();
        LetterDetailResponse response = letterSaveService.viewLetterBehindShare(userId, secretLetterKey);
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_503, response);
    }
}
