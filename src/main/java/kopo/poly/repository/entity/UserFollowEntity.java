package kopo.poly.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Table(name = "USER_FOLLOW")
@DynamicInsert // 값이 NULL이 아닌것만 INSERT함
@DynamicUpdate // 값이 NULL이 아닌것만 UPDATE함
@Builder
//@Cacheable
@Entity
@IdClass(FollowPK.class)
public class UserFollowEntity implements Serializable {

    @Id
    @Column(name = "follower_id")
    private String followerId;

    @Id
    @Column(name = "following_id")
    private String followingId;

    @Column(name = "reg_dt")
    private String regDt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", insertable = false, updatable = false)
    private UserInfoEntity userInfoFollowing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", insertable = false, updatable = false)
    private UserInfoEntity userInfoFollower;

}
