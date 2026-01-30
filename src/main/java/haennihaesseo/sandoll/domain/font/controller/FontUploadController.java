package haennihaesseo.sandoll.domain.font.controller;

import haennihaesseo.sandoll.domain.font.entity.enums.Bone;
import haennihaesseo.sandoll.domain.font.entity.enums.Distance;
import haennihaesseo.sandoll.domain.font.entity.enums.FontType;
import haennihaesseo.sandoll.domain.font.entity.enums.Situation;
import haennihaesseo.sandoll.domain.font.entity.enums.Writer;
import haennihaesseo.sandoll.domain.font.service.FontUploadService;
import haennihaesseo.sandoll.global.response.ApiResponse;
import haennihaesseo.sandoll.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Font Upload", description = "백엔드 폰트 업로드용 API")
@RestController
@RequestMapping("/api/font")
@RequiredArgsConstructor
@Slf4j
public class FontUploadController {

  private final FontUploadService fontUploadService;

  @Operation (
      summary = "[Font Upload] 폰트 파일 업로드 및 DB 저장",
      description = "여러 폰트 파일과 순서대로 폰트 이름, 저장 폴더 이름을 받아서 S3에 업로드하고 DB에 저장합니다."
  )
  @PostMapping(value = "/upload", consumes = "multipart/form-data")
  public ResponseEntity<ApiResponse<String>> uploadFonts(
      @RequestPart(value = "fonts") List<MultipartFile> fonts,
      @Schema(description = "업로드할 폰트 이름 리스트 (파일 순서와 동일해야 함)")
      @RequestParam("fontNames") List<String> fontNames,
      @Schema(description = "저장할 S3 폴더 이름 (local/server인 경우와 voice/context인 경우 분류 위해 존재)", example = "local/fonts/voice")
      @RequestParam("directory") String directory,
      @Schema(description = "폰트 타입 (예: VOICE, CONTEXT 등)", example = "CONTEXT")
      @RequestParam("type") FontType fontType,
      @RequestParam(value = "situation", required = false) Situation situation,
      @RequestParam(value = "distance", required = false) Distance distance,
      @RequestParam(value = "bone", required = false) Bone bone,
      @RequestParam(value = "writer", required = false) Writer writer


  ) {
    int uploadCount = 0;
    if(fontType.equals(FontType.VOICE)) uploadCount = fontUploadService.voiceFontUploadService(fonts, fontNames, directory);
    else if(fontType.equals(FontType.CONTEXT)) uploadCount = fontUploadService.contextFontUploadService(fonts, fontNames, directory, situation, distance, bone, writer);

    return ApiResponse.success(SuccessStatus.CREATED, uploadCount + "개의 폰트가 업로드되었습니다.");

  }

}
