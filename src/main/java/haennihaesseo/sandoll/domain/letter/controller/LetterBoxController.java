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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "LetterBox", description = "편지 보관함에서 조회 및 숨김 API")
@RestController
@RequestMapping("/api/letter")
@RequiredArgsConstructor
@Slf4j
public class LetterBoxController {

    private final LetterBoxService letterBoxService;

    @Operation(
            summary = "[2.1] 받은 편지 전체 리스트 조회"
    )
    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<ReceiveLetterResponse>>> getInbox(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "status") OrderStatus orderStatus
    ) {
        Long userId = userPrincipal.getUser().getUserId();
        List<ReceiveLetterResponse> responses = letterBoxService.getReceivedLettersByUser(userId, orderStatus);
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_201, responses);
    }

    @Operation(
            summary = "[2.2] 편지함의 편지 개별 조회"
    )
    @GetMapping("/user/{letterId}")
    public ResponseEntity<ApiResponse<LetterDetailResponse>> getLetterDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable(name = "letterId") Long letterId
    ){
        Long userId = userPrincipal.getUser().getUserId();
        LetterDetailResponse response = letterBoxService.getLetterDetailsByLetter(userId, letterId);
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_202, response);
    }

    @Operation(
            summary = "[2.3] 편지함에서 편지 숨김 처리"
    )
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteLetter(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid LetterDeleteRequest request
    ){
        Long userId = userPrincipal.getUser().getUserId();
        letterBoxService.hideLetter(userId, request.getType(), request.getLetterIds());
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_203);
    }

    @Operation(
            summary = "[2.4] 보낸 편지 전체 리스트 조회"
    )
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
