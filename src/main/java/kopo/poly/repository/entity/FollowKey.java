package kopo.poly.repository.entity;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Builder
@NoArgsConstructor
@Data
public class FollowKey implements Serializable {

    private String followerId;
    private String followingId;

    public FollowKey(String followerId, String followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
    }
}
