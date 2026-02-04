package haennihaesseo.sandoll.domain.letter.controller;

import haennihaesseo.sandoll.domain.deco.service.BgmService;
import haennihaesseo.sandoll.domain.letter.dto.request.LetterInfoRequest;
import haennihaesseo.sandoll.domain.letter.dto.response.WritingLetterContentResponse;
import haennihaesseo.sandoll.domain.letter.dto.response.VoiceAnalysisResponse;
import haennihaesseo.sandoll.domain.letter.dto.response.VoiceSaveResponse;
import haennihaesseo.sandoll.domain.letter.service.ContentEditService;
import haennihaesseo.sandoll.domain.letter.service.LetterContextService;
import haennihaesseo.sandoll.domain.letter.service.LetterService;
import haennihaesseo.sandoll.domain.letter.service.LetterVoiceService;
import haennihaesseo.sandoll.domain.letter.status.LetterSuccessStatus;
import haennihaesseo.sandoll.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Letter Write", description = "편지 작성 API")
@RestController
@RequestMapping("/api/letter")
@RequiredArgsConstructor
@Slf4j
public class LetterController {

  private final LetterService letterService;
  private final LetterVoiceService letterVoiceService;
  private final LetterContextService letterContextService;
  private final ContentEditService contentEditService;

  @Operation(summary = "[3.1] 녹음 파일 저장 및 STT 편지 내용 조회, 편지 작성 키 발급")
  @PostMapping(value = "/voice", consumes = "multipart/form-data")
  public ResponseEntity<ApiResponse<VoiceSaveResponse>> saveVoiceFile(
      @RequestPart(value = "voice") MultipartFile voiceFile
  ) {
    VoiceSaveResponse responses = letterService.saveVoiceFile(voiceFile);
    return ApiResponse.success(LetterSuccessStatus.SUCCESS_301, responses);
  }

  @Operation(
      summary = "[3.2] 편지 정보 입력 및 내용 수정"
  )
  @PatchMapping
  public ResponseEntity<ApiResponse<Void>> inputLetterInfo(
      @RequestHeader("letterId") String letterId,
      @RequestBody @Valid LetterInfoRequest request
  ) {
    contentEditService.inputLetterInfo(letterId, request);

    return ApiResponse.success(LetterSuccessStatus.SUCCESS_302);
  }

  @Operation(
      summary = "[3.3] 목소리 분석 요청 (추천 목소리 폰트 조회) "
  )
  @GetMapping("/voice")
  public ResponseEntity<ApiResponse<VoiceAnalysisResponse>> analyzeVoice(
      @RequestHeader("letterId") String letterId
  ) {
    letterContextService.contextAnalyze(letterId);
    VoiceAnalysisResponse response = letterVoiceService.analyzeVoice(letterId);
    return ApiResponse.success(LetterSuccessStatus.SUCCESS_303, response);
  }


  @Operation(
          summary = "[3.5] 전체 편지 내용 조회"
  )
  @GetMapping("/content")
  public ResponseEntity<ApiResponse<WritingLetterContentResponse>> getWritingLetterContent(
      @RequestHeader("letterId") String letterId
  ) {
    WritingLetterContentResponse response = letterService.getWritingLetterContent(letterId);
    return ApiResponse.success(LetterSuccessStatus.SUCCESS_305, response);
  }



}

