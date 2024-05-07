package kopo.poly.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserFollowDTO(
        String followerId,
        String followingId,
        String regDt,
        String userId,
        String userName,
        String profilePath

) {

}
