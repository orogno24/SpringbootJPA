package kopo.poly.dto;

import lombok.Builder;

@Builder
public record BookmarkDTO(

        Long bookmarkSeq,

        String userId,

        String regDt,

        String eventSeq,

        String eventTitle,

        String startDate,

        String endDate,

        String nSeq, // 달력 페이지에서 일정 클릭 시, 상세보기로 이동하기 위한 컬럼

        String existsYn

) {
}
