package kopo.poly.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.dto.WeatherDTO;
import kopo.poly.service.IWeatherFeign;
import kopo.poly.service.IWeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class WeatherService implements IWeatherService {

    private final IWeatherFeign weatherFeign;

    @Override
    public WeatherDTO getWeather(String city, String apiKey) throws JsonProcessingException {
        log.info("city : " + city);
        log.info("apiKey : " + apiKey);

        // API 호출
        String response = weatherFeign.getWeather(city, apiKey, "kr", "metric");
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> rootMap = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});

        // 필요한 정보 추출
        Map<String, Object> mainMap = (Map<String, Object>) rootMap.get("main");
        Map<String, Object> windMap = (Map<String, Object>) rootMap.get("wind");
        Map<String, Object> weatherMap = ((List<Map<String, Object>>) rootMap.get("weather")).get(0);

        String name = (String) rootMap.get("name");
        String description = (String) weatherMap.get("description");
        double temp = (Double) mainMap.get("temp");
        double feelsLike = (Double) mainMap.get("feels_like");
        int humidity = (Integer) mainMap.get("humidity");
        double windSpeed = (Double) windMap.get("speed");

        // DTO로 반환
        return WeatherDTO.builder()
                .name(name)
                .description(description)
                .temp(temp)
                .feelsLike(feelsLike)
                .humidity(humidity)
                .windSpeed(windSpeed)
                .build();
    }
}

