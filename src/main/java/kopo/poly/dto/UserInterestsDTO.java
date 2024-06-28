package kopo.poly.dto;

import lombok.Builder;

@Builder
public record UserInterestsDTO(
        int interestId,
        String userId,
        String keyword,
        String regDt
) {

}