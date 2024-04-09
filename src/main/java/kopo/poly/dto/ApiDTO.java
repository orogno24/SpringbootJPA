package kopo.poly.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record ApiDTO(

        @JsonProperty("CODENAME")
        String codename, // 분류
        @JsonProperty("GUNAME")
        String guName, // 자치구
        @JsonProperty("TITLE")
        String title, // 공연/행사명
        @JsonProperty("DATE")
        String date, // 날짜/시간
        @JsonProperty("PLACE")
        String place, // 장소
        @JsonProperty("ORG_NAME")
        String orgName, // 기관명
        @JsonProperty("USE_TRGT")
        String useTrgt, // 이용대상
        @JsonProperty("USE_FEE")
        String useFee, // 이용요금
        @JsonProperty("PLAYER")
        String player, // 출연자정보
        @JsonProperty("PROGRAM")
        String program, // 프로그램소개
        @JsonProperty("ETC_DESC")
        String etcDesc, // 기타내용
        @JsonProperty("ORG_LINK")
        String orgLink, // 홈페이지 주소
        @JsonProperty("MAIN_IMG")
        String mainImg, // 대표이미지
        @JsonProperty("RGSTDATE")
        String rgstDate, // 신청일
        @JsonProperty("TICKET")
        String ticket, // 시민/기관
        @JsonProperty("STRTDATE")
        String startDate, // 시작일
        @JsonProperty("END_DATE")
        String endDate, // 종료일
        @JsonProperty("THEMECODE")
        String themeCode, // 테마분류
        @JsonProperty("LOT")
        String lot, // 위도(X좌표)
        @JsonProperty("LAT")
        String lat, // 경도(Y좌표)
        @JsonProperty("IS_FREE")
        String isFree, // 유무료
        @JsonProperty("HMPG_ADDR")
        String hmpgAddr // 문화포털상세URL
) {

}