package kopo.poly.service.impl;

import kopo.poly.redis.IRedisMapper;
import kopo.poly.service.IRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisService implements IRedisService {

    private final IRedisMapper redisMapper;

    @Override
    public boolean getExistKey(String redisKey) throws Exception {
        return redisMapper.getExistKey(redisKey);
    }

}
