package kopo.poly.controller;

import kopo.poly.dto.WeatherDTO;
import kopo.poly.service.IWeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WeatherController {

    private final IWeatherService weatherService;

    @Value("${weather.api.region}")
    private String region;

    @Value("${weather.api.key}")
    private String key;

    @GetMapping("/weather")
    public WeatherDTO getWeather() throws Exception {
        return weatherService.getWeather(region, key);
    }
}