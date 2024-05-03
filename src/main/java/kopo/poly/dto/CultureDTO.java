package kopo.poly.dto;

import lombok.Builder;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

@Builder
public record CultureDTO(

        String id,
        String addr,
        String airport,
        String blue,
        String bussstop,
        String closeday,
        String entrFee,
        String entrfree,
        String etcDesc,
        String facDesc,
        String facName,
        String fax,
        String green,
        String homepage,
        GeoJsonPoint location,
        String mainImg,
        Integer num,
        String openDay,
        String openhour,
        String phne,
        String red,
        String seatCnt,
        String subjcode,
        String subway,
        Double xCoord,
        Double yCoord,
        String yellow

) {
}
