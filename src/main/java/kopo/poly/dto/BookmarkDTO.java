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

        String existsYn

) {
}
