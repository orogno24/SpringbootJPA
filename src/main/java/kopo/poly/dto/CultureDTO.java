package kopo.poly.dto;

import lombok.Builder;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Document // 해당 클래스의 인스턴스가 MongoDB 데이터베이스의 컬렉션에 저장될 수 있는 문서로 관리됨
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
        String yellow,
        Double radius

) {
}
