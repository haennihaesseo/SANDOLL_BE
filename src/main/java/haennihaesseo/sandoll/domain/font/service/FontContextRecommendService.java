package haennihaesseo.sandoll.domain.font.service;

import com.fasterxml.jackson.databind.JsonNode;
import haennihaesseo.sandoll.domain.font.entity.enums.Bone;
import haennihaesseo.sandoll.domain.font.entity.enums.Distance;
import haennihaesseo.sandoll.domain.font.entity.enums.Situation;
import haennihaesseo.sandoll.domain.font.entity.enums.Writer;
import org.springframework.stereotype.Service;

@Service
public class FontContextRecommendService {

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

    // todo 폰트 추천 로직 알고리즘 유틸


}
