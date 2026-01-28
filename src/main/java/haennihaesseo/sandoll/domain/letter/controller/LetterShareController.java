package haennihaesseo.sandoll.domain.letter.controller;

import haennihaesseo.sandoll.domain.letter.dto.request.LetterLinkViewRequest;
import haennihaesseo.sandoll.domain.letter.dto.response.LetterDetailResponse;
import haennihaesseo.sandoll.domain.letter.dto.response.SecretLetterKeyResponse;
import haennihaesseo.sandoll.domain.letter.service.LetterShareService;
import haennihaesseo.sandoll.domain.letter.status.LetterSuccessStatus;
import haennihaesseo.sandoll.global.auth.principal.UserPrincipal;
import haennihaesseo.sandoll.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/letter")
@RequiredArgsConstructor
@Slf4j
public class LetterShareController {

    private final LetterShareService letterShareService;

    @GetMapping("/{letterId}/share")
    public ResponseEntity<ApiResponse<SecretLetterKeyResponse>> getSecretLetterKey(
            @PathVariable(name = "letterId") Long letterId
    ) {
        SecretLetterKeyResponse response = letterShareService.getLetterSecretKeyByLetterId(letterId);
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_205, response);
    }

    @PostMapping("/view")
    public ResponseEntity<ApiResponse<LetterDetailResponse>> getLetterDetailByLink(
            @RequestBody LetterLinkViewRequest letterLinkViewRequest
    ){
        LetterDetailResponse response = letterShareService.getLetterDetailsByLink(letterLinkViewRequest.getSecretLetterId(), letterLinkViewRequest.getPassword());
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_601, response);
    }

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
