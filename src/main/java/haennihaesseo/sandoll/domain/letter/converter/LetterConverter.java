package haennihaesseo.sandoll.domain.letter.converter;

import haennihaesseo.sandoll.domain.font.entity.Font;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetter;
import haennihaesseo.sandoll.domain.letter.cache.CachedWord;
import haennihaesseo.sandoll.domain.letter.dto.response.VoiceAnalysisResponse;
import haennihaesseo.sandoll.domain.letter.dto.response.VoiceAnalysisResponse.RecommendFont;
import haennihaesseo.sandoll.domain.letter.dto.response.VoiceSaveResponse;
import haennihaesseo.sandoll.domain.letter.dto.response.LetterDetailResponse;
import haennihaesseo.sandoll.domain.letter.dto.response.WritingLetterContentResponse;
import haennihaesseo.sandoll.global.infra.python.dto.PythonVoiceAnalysisRequest;
import haennihaesseo.sandoll.global.infra.stt.SttResult;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LetterConverter {

  public List<CachedWord> toCachedWords(SttResult sttResult) {
    return sttResult.getWords().stream()
        .map(w -> CachedWord.builder()
            .word(w.getWord())
            .startTime(w.getStartTime())
            .endTime(w.getEndTime())
            .wordOrder((double) w.getOrder())
            .build())
        .toList();
  }

  public CachedLetter toCachedLetter(String letterId, String voiceUrl, SttResult sttResult, List<CachedWord> cachedWords) {
    return CachedLetter.builder()
        .letterId(letterId)
        .voiceUrl(voiceUrl)
        .duration(sttResult.getTotalDuration())
        .content(sttResult.getFullText())
        .words(cachedWords)
        .build();
  }

  public VoiceSaveResponse toVoiceSaveResponse(String letterId, String voiceUrl, SttResult sttResult) {
    return VoiceSaveResponse.builder()
        .letterId(letterId)
        .voiceUrl(voiceUrl)
        .duration(sttResult.getTotalDuration())
        .content(sttResult.getFullText())
        .build();
  }

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

  public WritingLetterContentResponse toWritingLetterContentResponse(CachedLetter cachedLetter, String fontName) {
    List<WritingLetterContentResponse.WritingWordInfo> wordInfos = cachedLetter.getWords().stream()
        .sorted(Comparator.comparingDouble(CachedWord::getWordOrder))
        .map(w -> WritingLetterContentResponse.WritingWordInfo.builder()
            .word(w.getWord())
            .startTime(w.getStartTime())
            .endTime(w.getEndTime())
            .build())
        .toList();

    return WritingLetterContentResponse.builder()
        .voiceUrl(cachedLetter.getVoiceUrl())
        .duration(cachedLetter.getDuration())
        .fontId(cachedLetter.getFontId())
        .fontName(fontName)
        .content(cachedLetter.getContent())
        .title(cachedLetter.getTitle())
        .sender(cachedLetter.getSender())
        .bgmUrl(cachedLetter.getBgmDto() != null ? cachedLetter.getBgmDto().bgmUrl() : null)
        .templateUrl(cachedLetter.getTemplateUrl())
        .words(wordInfos)
        .build();
  }
}