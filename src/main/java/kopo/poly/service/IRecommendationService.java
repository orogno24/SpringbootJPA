package kopo.poly.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kopo.poly.dto.ApiDTO;
import kopo.poly.dto.RedisDTO;

import java.util.List;

public interface IRecommendationService {

    public List<ApiDTO> getRecommendedEvents(RedisDTO redisDTO, ApiDTO pDTO, List<String> interestKeywords) throws JsonProcessingException;

}
