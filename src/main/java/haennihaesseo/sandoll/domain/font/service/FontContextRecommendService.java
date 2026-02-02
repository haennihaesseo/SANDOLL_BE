package haennihaesseo.sandoll.domain.font.service;

import com.fasterxml.jackson.databind.JsonNode;
import haennihaesseo.sandoll.domain.font.entity.Font;
import haennihaesseo.sandoll.domain.font.entity.enums.*;
import haennihaesseo.sandoll.domain.font.repository.FontRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FontContextRecommendService {
    private FontRepository fontRepository;

    /**
     * gemini로 부터 추출된 내용을 통해 폰트 추천
     * @param letterId
     * @param data
     */
    public void saveContextFontsInLetter(String letterId, JsonNode data){
        Bone bone = Bone.valueOf(data.get("bone").asText());
        Writer writer = Writer.valueOf(data.get("writer").asText());
        Situation situation = Situation.valueOf(data.get("situation").asText());
        Distance distance = Distance.valueOf(data.get("distance").asText());


    }

    private List<Font> recommendFonts(String letterId, Bone bone, Writer writer, Situation situation, Distance distance) {
        return fontRepository.findByMatchScore(bone, writer, situation, distance, 1);
    }

    // todo 추천된 폰트 캐시에 저장
    private void saveContextFontsInLetter(String letterId, List<Font> contextFonts){

    }

}
