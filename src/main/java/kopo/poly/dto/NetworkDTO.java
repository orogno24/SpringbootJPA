package kopo.poly.dto;

import lombok.Builder;

@Builder
public record NetworkDTO(
        Long networkSeq,
        String userId,
        String name,
        String contents,
        String dateTime,
        Long maxParticipants,
        String eventSeq,
        String eventName,
        String imagePath,
        String regDt,
        String userName,
        String profilePath,

        String type
) {}
