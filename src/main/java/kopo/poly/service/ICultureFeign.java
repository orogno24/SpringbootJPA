package kopo.poly.service;

import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "ICultureFeign", url = "http://openapi.seoul.go.kr:8088")
public interface ICultureFeign {
    @RequestLine("GET /{apiKey}/{type}/culturalSpaceInfo/{startIndex}/{endIndex}")
    String getCultureApi(
            @Param("apiKey") String apiKey,
            @Param("type") String type,
            @Param("startIndex") int startIndex,
            @Param("endIndex") int endIndex
    );
}
