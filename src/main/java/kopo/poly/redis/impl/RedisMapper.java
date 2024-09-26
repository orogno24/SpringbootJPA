package kopo.poly.redis.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.dto.RedisDTO;
import kopo.poly.redis.IRedisMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisMapper implements IRedisMapper {

    private final RedisTemplate<String, Object> redisDB;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * ReidsDB 저장된 키 삭제하는 공통 함수
     */
    private void deleteRedisKey(String redisKey) {

        if (redisDB.hasKey(redisKey)) { // 데이터가 존재하면, 기존 데이터 삭제하기

            redisDB.delete(redisKey); // 데이터 삭제

            log.info("삭제 성공!");

        }
    }

    /**
     * ReidsDB에 문화행사 리스트 저장
     */
    @Override
    public void insertEventList(List<Map<String, Object>> rContent, String colNm) throws Exception {

        log.info(this.getClass().getName() + ".insertEventList Start!");

        redisDB.setKeySerializer(new StringRedisSerializer());
        redisDB.setValueSerializer(new StringRedisSerializer());

        this.deleteRedisKey(colNm); // RedisDB 저장된 키 삭제

        // 데이터 저장하기
        String jsonString = convertToJson(rContent);
        redisDB.opsForValue().set(colNm, jsonString);

        // 저장된 데이터는 1시간동안 보관하기
        // 1시간이 지나면, 자동으로 데이터가 삭제되도록 설정함
        redisDB.expire(colNm, 60, TimeUnit.MINUTES);

        log.info(this.getClass().getName() + ".insertEventList End!");

    }

    /**
     * Json으로 변환
     */
    private String convertToJson(List<Map<String, Object>> rContent) throws JsonProcessingException {
        return objectMapper.writeValueAsString(rContent);
    }

    /**
     * ReidsDB에 동일한 이름의 컬렉션이 있는지 조회
     */
    @Override
    public boolean getExistKey(String colNm) throws Exception {
        boolean getExistKey = redisDB.hasKey(colNm);
        log.info("Checking existence of colNm '{}': {}", colNm, getExistKey);
        return getExistKey;
    }

    /**
     * ReidsDB에서 문화행사 리스트 가져오기
     */
    @Override
    public RedisDTO getEventList(String colNm) throws Exception {
        log.info(this.getClass().getName() + ".getEventList Start!");

        // redisDB의 키의 데이터 타입을 String으로 정의(항상 String으로 설정함)
        redisDB.setKeySerializer(new StringRedisSerializer());

        // RedisDTO에 저장된 데이터를 자동으로 JSON으로 변경하기
        redisDB.setValueSerializer(new StringRedisSerializer());

        RedisDTO rDTO = null;

        if (redisDB.hasKey(colNm)) { // 데이터가 존재하면, 조회하기
            String contents = (String) redisDB.opsForValue().get(colNm); // redisKey 통해 조회하기
            rDTO = RedisDTO.builder()
                    .contents(contents)
                    .build();
        }

        log.info(this.getClass().getName() + ".getEventList End!");

        return rDTO;
    }

}
