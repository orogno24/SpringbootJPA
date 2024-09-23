package kopo.poly.service;

import feign.RequestLine;
import feign.Param;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "IWeatherFeign", url = "http://api.openweathermap.org/data/2.5")
public interface IWeatherFeign {
    @RequestLine("GET /weather?q={q}&appid={appid}&lang={lang}&units={units}")
    String getWeather(
            @Param("q") String city,
            @Param("appid") String apiKey,
            @Param("lang") String lang,
            @Param("units") String units
    );
}
