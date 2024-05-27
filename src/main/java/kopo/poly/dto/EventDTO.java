package kopo.poly.dto;

import lombok.Builder;

@Builder
public record EventDTO(
        String codename, // 분류
        String guname, // 자치구
        String title, // 공연/행사명
        String date, // 날짜/시간
        String place, // 장소
        String orgName, // 기관명
        String useTrgt, // 이용대상
        String useFee, // 이용요금
        String player, // 출연자정보
        String program, // 프로그램소개
        String etcDesc, // 기타내용
        String orgLink, // 홈페이지 주소
        String mainImg, // 대표이미지
        String rgstdate, // 신청일
        String ticket, // 시민/기관
        String strtdate, // 시작일
        String endDate, // 종료일
        String themecode, // 테마분류
        Double lot, // 위도(X좌표)
        Double lat, // 경도(Y좌표)
        String isFree, // 유무료
        String hmpgAddr // 문화포털상세URL
) {

}
