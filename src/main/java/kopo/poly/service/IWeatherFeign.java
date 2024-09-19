package kopo.poly.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "IWeatherFeign", url = "http://api.openweathermap.org/data/2.5")
public interface IWeatherFeign {
    @GetMapping("/weather")
    String getWeather(
            @RequestParam("q") String city,
            @RequestParam("appid") String apiKey,
            @RequestParam("lang") String lang,
            @RequestParam("units") String units
    );
}
