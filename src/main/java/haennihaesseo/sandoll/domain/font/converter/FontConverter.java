package haennihaesseo.sandoll.domain.font.converter;

import haennihaesseo.sandoll.domain.font.dto.response.RecommendFontResponse;
import haennihaesseo.sandoll.domain.font.dto.response.RefreshFontResponse;
import haennihaesseo.sandoll.domain.font.dto.response.RefreshFontResponse.RecommendFont;
import haennihaesseo.sandoll.domain.font.entity.Font;
import haennihaesseo.sandoll.domain.font.entity.enums.FontType;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FontConverter {

  public RefreshFontResponse toRefreshFontResponse(FontType type, List<Font> fonts,
      List<String> keywords) {
    List<RecommendFont> fontDtos = toRecommendFontList(fonts, keywords);

    return RefreshFontResponse.builder()
        .type(type)
        .fonts(fontDtos)
        .build();
  }

  public List<RecommendFont> toRecommendFontList(List<Font> font, List<String> keywords) {
    return font.stream()
        .map(f -> RecommendFont.builder()
            .fontId(f.getFontId())
            .name(f.getName())
            .fontUrl(f.getFontUrl())
            .keywords(keywords)
            .build())
        .toList();
  }

  public RecommendFontResponse toRecommendFontResponse(List<RecommendFont> voiceFont, List<RecommendFont> contextFont) {
    return RecommendFontResponse.builder()
        .voiceFonts(voiceFont)
        .contextFonts(contextFont)
        .build();
  }
}
