package haennihaesseo.sandoll.domain.deco.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.GeneralException;
import haennihaesseo.sandoll.domain.deco.converter.DecoConverter;
import haennihaesseo.sandoll.domain.deco.dto.response.BgmsResponse;
import haennihaesseo.sandoll.domain.deco.entity.Bgm;
import haennihaesseo.sandoll.domain.deco.repository.BgmRepository;
import haennihaesseo.sandoll.domain.deco.status.DecoErrorStatus;
import haennihaesseo.sandoll.global.exception.GlobalException;
import haennihaesseo.sandoll.global.infra.RedisClient;
import haennihaesseo.sandoll.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BgmService {

    private final BgmRepository bgmRepository;
    private final RedisClient redisClient;
    private final DecoConverter decoConverter;
    private final ObjectMapper objectMapper;

    /**
     * bgm 생성 api와 통신
     * @param letterKey
     */
    public void createBgmsByLetter(String letterKey) {
        // todo bgm 생성 api로 통신 요청 (비동기 처리 예정)
    }

    /**
     * 생성된 bgm 리스트 조회
     * @param letterKey
     * @return bgm객체 리스트
     */
    public BgmsResponse getBgmsByLetterKey(String letterKey) {

        // redis에서 편지 내용 바탕으로 생성된 bgm 정보 가져오기
        String jsonBgms = redisClient.getData("bgms", letterKey);
        if (jsonBgms == null) {
            throw new GlobalException(DecoErrorStatus.BGM_GENERATING);
        }
        try {
            List<BgmsResponse.BgmDto> bgmDtos = objectMapper.readValue(jsonBgms, new TypeReference<List<BgmsResponse.BgmDto>>() {});
            return BgmsResponse.builder().bgms(bgmDtos).build();
        } catch (JsonProcessingException e) {
            throw new GlobalException(ErrorStatus.JSON_PARSING_FAIL);
        }
    }

    /**
     * 유저가 선택한 bgm 편지에 저장
     * @param letterKey
     * @param bgmId
     */
    // todo 편지정보 Redis에 저장되는 구조보고 수정
    public void saveBgmOnLetter(String letterKey, Long bgmId) {
    }

    // createBgmsByLetter에서 이용
    private void saveBgmAtRedis(String letterKey, List<BgmsResponse.BgmDto> bgmDtos) throws JsonProcessingException {
        redisClient.setData("bgms", letterKey, objectMapper.writeValueAsString(bgmDtos), 3000);
    }
}
