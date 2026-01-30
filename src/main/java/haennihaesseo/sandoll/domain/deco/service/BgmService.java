package haennihaesseo.sandoll.domain.deco.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import haennihaesseo.sandoll.domain.deco.converter.DecoConverter;
import haennihaesseo.sandoll.domain.deco.dto.response.BgmsResponse;
import haennihaesseo.sandoll.domain.deco.exception.DecoException;
import haennihaesseo.sandoll.domain.deco.repository.BgmRepository;
import haennihaesseo.sandoll.domain.deco.status.DecoErrorStatus;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetter;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetterRepository;
import haennihaesseo.sandoll.domain.letter.exception.LetterException;
import haennihaesseo.sandoll.domain.letter.status.LetterErrorStatus;
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

    private final RedisClient redisClient;
    private final ObjectMapper objectMapper;
    private final CachedLetterRepository cachedLetterRepository;

    /**
     * bgm 생성 api와 통신
     * @param letterId
     */
    public void createBgmsByLetter(String letterId) {
        // todo bgm 생성 api로 통신 요청 (비동기 처리 예정)
        List<BgmsResponse.BgmDto> bgmList = List.of(
                BgmsResponse.BgmDto.builder()
                        .bgmId(1L)
                        .bgmUrl("https://cdn.sandoll.com/audio/spring-breeze.mp3")
                        .keyword(List.of("차분한", "봄", "어쿠스틱"))
                        .name("봄바람의 속삭임")
                        .build(),
                BgmsResponse.BgmDto.builder()
                        .bgmId(2L)
                        .bgmUrl("https://cdn.sandoll.com/audio/lofi-study.mp3")
                        .keyword(List.of("집중", "Lo-fi", "비트"))
                        .name("한밤중의 도서관")
                        .build(),
                BgmsResponse.BgmDto.builder()
                        .bgmId(3L)
                        .bgmUrl("https://cdn.sandoll.com/audio/summer-ocean.mp3")
                        .keyword(List.of("시원한", "바다", "활기찬"))
                        .name("파도 소리 서핑")
                        .build()
        );

        try {
            saveBgmAtRedis(letterId, bgmList);
        } catch (JsonProcessingException e) {
            throw new GlobalException(ErrorStatus.JSON_PARSING_FAIL);
        }
    }

    /**
     * 생성된 bgm 리스트 조회
     * @param letterId
     * @return bgm객체 리스트
     */
    public BgmsResponse getBgmsByLetterId(String letterId) {
        // redis에서 편지 내용 바탕으로 생성된 bgm 정보 가져오기
        String jsonBgms = redisClient.getData("bgms", letterId);
        if (jsonBgms == null) {
            throw new DecoException(DecoErrorStatus.BGM_GENERATING);
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
     * @param letterId
     * @param bgmId
     */
    public void saveBgmOnLetter(String letterId, Long bgmId) {
        CachedLetter cachedLetter = cachedLetterRepository.findById(letterId)
                .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

        if (bgmId == null) {
            cachedLetter.setBgmUrl(null);
            cachedLetterRepository.save(cachedLetter);
        } else {
            String jsonBgms = redisClient.getData("bgms", letterId);

            if (jsonBgms == null) {
                throw new DecoException(DecoErrorStatus.BGM_NOT_FOUND);
            }
            try {
                List<BgmsResponse.BgmDto> bgmDtos = objectMapper.readValue(jsonBgms, new TypeReference<List<BgmsResponse.BgmDto>>() {});
                BgmsResponse.BgmDto bgmDto = bgmDtos.stream()
                        .filter(dto -> dto.bgmId().equals(bgmId))
                        .findFirst()
                        .orElseThrow(() -> new DecoException(DecoErrorStatus.BGM_NOT_FOUND));

                cachedLetter.setBgmUrl(bgmDto.bgmUrl());
                cachedLetterRepository.save(cachedLetter);

            } catch (JsonProcessingException e) {
                throw new GlobalException(ErrorStatus.JSON_PARSING_FAIL);
            }
        }
    }

    private void saveBgmAtRedis(String letterId, List<BgmsResponse.BgmDto> bgmDtos) throws JsonProcessingException {
        redisClient.setData("bgms", letterId, objectMapper.writeValueAsString(bgmDtos), 3000);
    }
}
