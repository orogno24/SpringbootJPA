package kopo.poly.repository;

import kopo.poly.repository.entity.NoticeImageEntity;
import kopo.poly.repository.entity.UserInterestsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInterestsRepository extends JpaRepository<UserInterestsEntity, String> {

    /**
     * 특정 userId에 대한 모든 키워드 찾기
     *
     * @param userId 회원 아이디
     */
    List<UserInterestsEntity> findByUserId(String userId);

    /**
     * 특정 userId에 대한 모든 키워드 삭제
     *
     * @param userId 회원 아이디
     */
    void deleteByUserId(String userId);

}
