package kopo.poly.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Document // 해당 클래스의 인스턴스가 MongoDB 데이터베이스의 컬렉션에 저장될 수 있는 문서로 관리됨
public record CultureDTO(

        @JsonProperty("NUM")
        Integer num, // 번호

        @JsonProperty("SUBJCODE")
        String subjcode, // 주제분류

        @JsonProperty("FAC_NAME")
        String facName, // 문화시설명

        @JsonProperty("ADDR")
        String addr, // 주소

        @JsonProperty("X_COORD")
        Double xCoord, // 위도

        @JsonProperty("Y_COORD")
        Double yCoord, // 경도

        @JsonProperty("PHNE")
        String phne, // 전화번호

        @JsonProperty("FAX")
        String fax, // 팩스번호

        @JsonProperty("HOMEPAGE")
        String homepage, // 홈페이지

        @JsonProperty("OPENHOUR")
        String openhour, // 관람시간

        @JsonProperty("ENTR_FEE")
        String entrFee, // 관람료

        @JsonProperty("CLOSEDAY")
        String closeday, // 휴관일

        @JsonProperty("OPEN_DAY")
        String openDay, // 개관일자

        @JsonProperty("SEAT_CNT")
        String seatCnt, // 객석수

        @JsonProperty("MAIN_IMG")
        String mainImg, // 대표이미지

        @JsonProperty("ETC_DESC")
        String etcDesc, // 기타사항

        @JsonProperty("FAC_DESC")
        String facDesc, // 시설소개

        @JsonProperty("ENTRFREE")
        String entrfree, // 무료구분

        @JsonProperty("SUBWAY")
        String subway, // 지하철

        @JsonProperty("BUSSTOP")
        String bussstop, // 버스정거장

        @JsonProperty("YELLOW")
        String yellow, // YELLOW

        @JsonProperty("GREEN")
        String green, // GREEN

        @JsonProperty("BLUE")
        String blue, // BLUE

        @JsonProperty("RED")
        String red, // RED

        @JsonProperty("AIRPORT")
        String airport, // 공항버스

        GeoJsonPoint location, // 위치
        Double radius // 반경
) {
}
