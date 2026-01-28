package haennihaesseo.sandoll.domain.letter.controller;

import haennihaesseo.sandoll.domain.letter.dto.request.LetterDeleteRequest;
import haennihaesseo.sandoll.domain.letter.dto.response.LetterDetailResponse;
import haennihaesseo.sandoll.domain.letter.dto.request.OrderStatus;
import haennihaesseo.sandoll.domain.letter.dto.response.ReceiveLetterResponse;
import haennihaesseo.sandoll.domain.letter.dto.response.SendLetterResponse;
import haennihaesseo.sandoll.global.auth.principal.UserPrincipal;
import haennihaesseo.sandoll.domain.letter.service.LetterBoxService;
import haennihaesseo.sandoll.domain.letter.status.LetterSuccessStatus;
import haennihaesseo.sandoll.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/letter")
@RequiredArgsConstructor
@Slf4j
public class LetterBoxController {

    private final LetterBoxService letterBoxService;

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<ReceiveLetterResponse>>> getInbox(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "status") OrderStatus orderStatus
    ) {
        Long userId = userPrincipal.getUser().getUserId();
        List<ReceiveLetterResponse> responses = letterBoxService.getReceivedLettersByUser(userId, orderStatus);
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_201, responses);
    }

    @GetMapping("/user/{letterId}")
    public ResponseEntity<ApiResponse<LetterDetailResponse>> getLetterDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable(name = "letterId") Long letterId
    ){
        LetterDetailResponse response = letterBoxService.getLetterDetailsByLetter(letterId);
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_202, response);
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteLetter(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody LetterDeleteRequest request
    ){
        Long userId = userPrincipal.getUser().getUserId();
        letterBoxService.hideLetter(userId, request.getType(), request.getLetterIds());
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_203);
    }

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<List<SendLetterResponse>>> getOutbox(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "status") OrderStatus orderStatus
    ){
        Long userId = userPrincipal.getUser().getUserId();
        List<SendLetterResponse> responses = letterBoxService.getSentLettersByUser(userId, orderStatus);
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_204, responses);
    }
}
