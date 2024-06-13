package kopo.poly.dto;

import lombok.Builder;

@Builder
public record ScheduleDTO(

        Long scheduleSeq,

        String userId,

        String regDt,

        String eventSeq,

        String eventTitle,

        String startDate,

        String endDate,

        String nSeq, // 달력 페이지에서 일정 클릭 시, 상세보기로 이동하기 위함

        String existsYn

) {
}
