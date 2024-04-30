package kopo.poly.dto;

import lombok.Builder;

@Builder
public record CommentDTO(
        Long commentSeq,
        Long noticeSeq,
        String userId,
        String contents,
        String regDt,
        String userName,
        String profilePath
) {
}
