package kopo.poly.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record NetworkDTO(
        int networkSeq,
        String userId,
        String name,
        String contents,
        LocalDateTime dateTime,
        int maxParticipants,
        String eventSeq,
        String cultureSeq
) {}
