package kopo.poly.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record WeatherDTO(
        String name,
        String country,
        String description,
        double temp,
        double feelsLike,
        int humidity,
        double windSpeed
) {
}
