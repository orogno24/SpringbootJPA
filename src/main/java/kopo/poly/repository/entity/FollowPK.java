package kopo.poly.repository.entity;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Builder
@NoArgsConstructor
public class FollowPK implements Serializable {

    private String followerId;
    private String followingId;

    public FollowPK(String followerId, String followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
    }
}
