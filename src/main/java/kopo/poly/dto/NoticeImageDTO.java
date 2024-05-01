package kopo.poly.dto;

import lombok.Builder;

@Builder
public record NoticeImageDTO(
        Long imageSeq,
        Long noticeSeq,
        String imagePath

) {

}