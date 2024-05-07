package kopo.poly.repository;

import kopo.poly.repository.entity.FollowKey;
import kopo.poly.repository.entity.UserFollowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollowEntity, FollowKey> {

    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);

}
