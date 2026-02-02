package haennihaesseo.sandoll.domain.font.dto.response;

import haennihaesseo.sandoll.domain.font.entity.enums.FontType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RefreshFontResponse {
  private FontType type;
  private List<RecommendFont> fonts;

  @Builder
  public record RecommendFont(Long fontId, String name, String fontUrl, List<String> keywords) {}

}
