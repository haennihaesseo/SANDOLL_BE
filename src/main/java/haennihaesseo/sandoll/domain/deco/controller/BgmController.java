package haennihaesseo.sandoll.domain.deco.controller;

import haennihaesseo.sandoll.domain.deco.dto.response.BgmsResponse;
import haennihaesseo.sandoll.domain.deco.service.BgmService;
import haennihaesseo.sandoll.domain.deco.status.DecoSuccessStatus;
import haennihaesseo.sandoll.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deco/bgm")
@RequiredArgsConstructor
public class BgmController {

    private BgmService bgmService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createBgms(
            @RequestHeader(name = "letterKey") String letterKey
    ) {
        bgmService.createBgmsByLetter(letterKey);
        return ApiResponse.success(DecoSuccessStatus.SUCCESS_403);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<BgmsResponse>> getAllBgms(
            @RequestHeader(name = "letterKey") String letterKey
    ) {
        BgmsResponse response = bgmService.getBgmsByLetterKey(letterKey);
        return ApiResponse.success(DecoSuccessStatus.SUCCESS_404, response);
    }

    @PostMapping("/select")
    public ResponseEntity<ApiResponse<Void>> selectBgms(
            @RequestHeader(name = "letterKey") String letterKey,
            @RequestParam(name = "bgmId", required = false) Long bgmId
    ){
        bgmService.saveBgmOnLetter(letterKey, bgmId);
        return ApiResponse.success(DecoSuccessStatus.SUCCESS_405);
    }
}
