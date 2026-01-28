package haennihaesseo.sandoll.domain.letter.controller;

import haennihaesseo.sandoll.global.auth.principal.UserPrincipal;
import haennihaesseo.sandoll.domain.letter.dto.*;
import haennihaesseo.sandoll.domain.letter.service.LetterService;
import haennihaesseo.sandoll.domain.letter.status.LetterSuccessStatus;
import haennihaesseo.sandoll.domain.user.entity.User;
import haennihaesseo.sandoll.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/letter")
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<ReceiveLetterResponse>>> getInbox(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "status") OrderStatus orderStatus
    ) {
        Long userId = userPrincipal.getUser().getUserId();
        List<ReceiveLetterResponse> responses = letterService.getReceivedLettersByUser(userId, orderStatus);
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_201, responses);
    }

    @GetMapping("/{letterId}")
    public ResponseEntity<ApiResponse<LetterDetailResponse>> getLetterDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable(name = "letterId") Long letterId
    ){
        LetterDetailResponse response = letterService.getLetterDetailsByLetter(letterId);
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_202, response);
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteLetter(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody LetterDeleteRequest request
    ){
        Long userId = userPrincipal.getUser().getUserId();
        letterService.hideLetter(userId, request.getType(), request.getLetterIds());
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_203);
    }

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<List<SendLetterResponse>>> getOutbox(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "status") OrderStatus orderStatus
    ){
        Long userId = userPrincipal.getUser().getUserId();
        List<SendLetterResponse> responses = letterService.getSentLettersByUser(userId, orderStatus);
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_204, responses);
    }
}
