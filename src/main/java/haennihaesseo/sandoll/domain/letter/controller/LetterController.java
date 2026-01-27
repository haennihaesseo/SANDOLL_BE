package haennihaesseo.sandoll.domain.letter.controller;

import haennihaesseo.sandoll.domain.letter.dto.*;
import haennihaesseo.sandoll.domain.letter.service.LetterService;
import haennihaesseo.sandoll.domain.letter.status.LetterSuccessStatus;
import haennihaesseo.sandoll.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/letter")
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<ReceiveLetterResponse>>> getInbox(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "status") OrderStatus orderStatus
    ) {
        List<ReceiveLetterResponse> responses = letterService.getReceivedLettersByUser(userId, orderStatus);
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_201, responses);
    }

    @GetMapping("/{letterId}")
    public ResponseEntity<ApiResponse<LetterDetailResponse>> getLetterDetail(
            @PathVariable(name = "letterId") Long letterId
    ){
        LetterDetailResponse response = letterService.getLetterDetailsByLetter(letterId);
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_202, response);
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteLetter(
            @RequestParam(name = "userId") Long userId,
            @RequestBody LetterDeleteRequest request
    ){
        letterService.hideLetter(userId, request.getType(), request.getLetterIds());
        return ApiResponse.success(LetterSuccessStatus.SUCCESS_203);
    }

}
