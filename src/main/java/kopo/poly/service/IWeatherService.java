package kopo.poly.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kopo.poly.dto.ApiDTO;
import kopo.poly.dto.NoticeDTO;
import kopo.poly.dto.RedisDTO;
import kopo.poly.dto.WeatherDTO;

import java.util.List;

public interface IWeatherService {

    WeatherDTO getWeather(String city, String apiKey) throws Exception;

}

