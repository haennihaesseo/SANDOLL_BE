package haennihaesseo.sandoll.domain.letter.converter;

import haennihaesseo.sandoll.domain.font.entity.Font;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetter;
import haennihaesseo.sandoll.domain.letter.dto.response.VoiceAnalysisResponse;
import haennihaesseo.sandoll.domain.letter.dto.response.VoiceAnalysisResponse.RecommendFont;
import haennihaesseo.sandoll.global.infra.python.dto.PythonVoiceAnalysisRequest;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LetterVoiceConverter {

  public PythonVoiceAnalysisRequest toAnalysisRequest(CachedLetter cachedLetter) {
    List<PythonVoiceAnalysisRequest.WordData> wordDataList = cachedLetter.getWords().stream()
        .map(w -> PythonVoiceAnalysisRequest.WordData.builder()
            .word(w.getWord())
            .startTime(w.getStartTime())
            .endTime(w.getEndTime())
            .build())
        .toList();

    return PythonVoiceAnalysisRequest.builder()
        .voiceUrl(cachedLetter.getVoiceUrl())
        .content(cachedLetter.getContent())
        .words(wordDataList)
        .build();
  }

  public VoiceAnalysisResponse toVoiceAnalysisResponse(String analysisResult, List<Font> fonts) {
    List<RecommendFont> recommendFonts = fonts.stream()
        .map(font -> RecommendFont.builder()
            .fontId(font.getFontId())
            .name(font.getName())
            .fontUrl(font.getFontUrl())
            .build())
        .toList();

    return VoiceAnalysisResponse.builder()
        .result(analysisResult)
        .fonts(recommendFonts)
        .build();
  }
}