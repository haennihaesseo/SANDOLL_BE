package haennihaesseo.sandoll.domain.letter.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LetterInfoRequest {
  @NotBlank
  private String title;

  @NotBlank
  private String sender;

  @NotBlank
  private String content;
}
