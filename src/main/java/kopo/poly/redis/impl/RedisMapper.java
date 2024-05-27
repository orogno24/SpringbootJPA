package kopo.poly.redis.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.redis.IRedisMapper;
import kopo.poly.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisMapper implements IRedisMapper {

    private final RedisTemplate<String, Object> redisDB;

    @Override
    public int insertEventList(List<Map<String, Object>> rContent, String colNm) throws Exception {

        log.info(this.getClass().getName() + ".insertEventList Start!");

        int res;

        redisDB.setKeySerializer(new StringRedisSerializer());
        redisDB.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // 람다식으로 데이터 저장하기
        rContent.forEach(event -> {
            try {
                redisDB.opsForList().leftPush(colNm, event);
            } catch (Exception e) {
                log.error("Error pushing event to Redis", e);
            }
        });

        // 저장된 데이터는 1시간동안 보관하기
        redisDB.expire(colNm, 1, TimeUnit.HOURS);

        res = 1;

        log.info(this.getClass().getName() + ".insertEventList End!");

        return res;
    }

    @Override
    public boolean getExistKey(String colNm) throws Exception {
        boolean getExistKey = redisDB.hasKey(colNm);
        log.info("Checking existence of colNm '{}': {}", colNm, getExistKey);
        return getExistKey;
    }

    @Override
    public List<Map<String, Object>> getEventList(String colNm) throws Exception {
        log.info(this.getClass().getName() + ".getEventList Start!");

        redisDB.setKeySerializer(new StringRedisSerializer());
        redisDB.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        List<Map<String, Object>> rContent = new ArrayList<>();

        // Redis에서 리스트 형태로 데이터 조회
        List<Object> rawList = redisDB.opsForList().range(colNm, 0, -1);
        if (rawList != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            for (Object item : rawList) {
                Map<String, Object> map = objectMapper.convertValue(item, new TypeReference<Map<String, Object>>() {});
                rContent.add(map);
            }
        }

        // 저장된 데이터는 1시간동안 연장하기
        redisDB.expire(colNm, 1, TimeUnit.HOURS);

        log.info(this.getClass().getName() + ".getEventList End!");

        return rContent;
    }

}
