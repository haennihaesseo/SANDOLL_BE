package haennihaesseo.sandoll.domain.deco.controller;

import haennihaesseo.sandoll.domain.deco.dto.response.BgmsResponse;
import haennihaesseo.sandoll.domain.deco.service.BgmService;
import haennihaesseo.sandoll.domain.deco.status.DecoSuccessStatus;
import haennihaesseo.sandoll.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deco/bgm")
@RequiredArgsConstructor
public class BgmController {

    private final BgmService bgmService;

//    @Operation(
//            summary = "[4.3] 백엔드에서 테스트시만 사용"
//    )
//    @PostMapping
//    public ResponseEntity<ApiResponse<Void>> createBgms(
//            @RequestHeader(name = "letterId") String letterId
//    ) {
//        bgmService.createBgmsByLetter(letterId);
//        return ApiResponse.success(DecoSuccessStatus.SUCCESS_403);
//    }

    @Operation(
            summary = "[4.4] 생성된 Bgm 조회"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<BgmsResponse>> getAllBgms(
            @RequestHeader(name = "letterId") String letterId
    ) {
        BgmsResponse response = bgmService.getBgmsByLetterId(letterId);
        return ApiResponse.success(DecoSuccessStatus.SUCCESS_404, response);
    }

    @Operation(
            summary = "[4.5] Bgm 적용"
    )
    @PostMapping("/select")
    public ResponseEntity<ApiResponse<Void>> selectBgms(
            @RequestHeader(name = "letterId") String letterId,
            @RequestParam(name = "bgmId", required = false) Long bgmId,
            @RequestParam(name = "bgmSize", required = false) Double bgmSize
    ){
        bgmService.saveBgmOnLetter(letterId, bgmId, bgmSize);
        return ApiResponse.success(DecoSuccessStatus.SUCCESS_405);
    }
}
