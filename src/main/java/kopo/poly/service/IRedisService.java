package kopo.poly.service;

public interface IRedisService {

    boolean getExistKey(String redisKey) throws Exception;

}
