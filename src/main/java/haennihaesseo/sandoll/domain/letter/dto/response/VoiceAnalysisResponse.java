package haennihaesseo.sandoll.domain.letter.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class VoiceAnalysisResponse {
  private String result;
  private List<RecommendFont> fonts;

  @Builder
  public record RecommendFont(Long fontId, String name, String fontUrl) {}
}
