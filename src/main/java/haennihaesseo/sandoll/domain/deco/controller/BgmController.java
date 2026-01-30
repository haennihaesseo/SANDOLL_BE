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

    private final BgmService bgmService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createBgms(
            @RequestHeader(name = "letterId") String letterId
    ) {
        bgmService.createBgmsByLetter(letterId);
        return ApiResponse.success(DecoSuccessStatus.SUCCESS_403);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<BgmsResponse>> getAllBgms(
            @RequestHeader(name = "letterId") String letterId
    ) {
        BgmsResponse response = bgmService.getBgmsByLetterId(letterId);
        return ApiResponse.success(DecoSuccessStatus.SUCCESS_404, response);
    }

    @PostMapping("/select")
    public ResponseEntity<ApiResponse<Void>> selectBgms(
            @RequestHeader(name = "letterId") String letterId,
            @RequestParam(name = "bgmId", required = false) Long bgmId
    ){
        bgmService.saveBgmOnLetter(letterId, bgmId);
        return ApiResponse.success(DecoSuccessStatus.SUCCESS_405);
    }
}
