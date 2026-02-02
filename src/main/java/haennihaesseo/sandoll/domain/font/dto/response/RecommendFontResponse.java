package haennihaesseo.sandoll.domain.font.dto.response;

import haennihaesseo.sandoll.domain.font.dto.response.RefreshFontResponse.RecommendFont;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RecommendFontResponse {
  List<RecommendFont> voiceFonts;
  List<RecommendFont> contextFonts;
}
