package kopo.poly.repository;

import kopo.poly.repository.entity.NoticeSQLEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeSQLRepository extends JpaRepository<NoticeSQLEntity, Long> {
    @Query(value = "SELECT N.NOTICE_SEQ, N.TITLE, N.NOTICE_YN, N.READ_CNT, N.USER_ID, N.REG_ID, N.REG_DT, N.CHG_ID, N.CHG_DT, " +
            "U.USER_NAME, U.PROFILE_PATH, I.IMAGE_PATH " +
            "FROM myDB.NOTICE N JOIN myDB.USER_INFO U ON N.USER_ID = U.USER_ID LEFT JOIN ( " +
            "SELECT NOTICE_SEQ, IMAGE_PATH, ROW_NUMBER() OVER (PARTITION BY NOTICE_SEQ ORDER BY IMAGE_SEQ) AS rn FROM myDB.NOTICE_IMAGE) I " +
            "ON N.NOTICE_SEQ = I.NOTICE_SEQ AND I.rn = 1 " +
            "ORDER BY N.NOTICE_YN DESC, N.NOTICE_SEQ DESC",
            nativeQuery = true)
    List<NoticeSQLEntity> getNoticeListUsingSQL();

    @Query(value = "SELECT N.NOTICE_SEQ, N.TITLE, N.NOTICE_YN, N.READ_CNT, N.USER_ID, N.REG_ID, N.REG_DT, N.CHG_ID, N.CHG_DT, " +
            "U.USER_NAME, U.PROFILE_PATH, I.IMAGE_PATH " +
            "FROM myDB.NOTICE N JOIN myDB.USER_INFO U ON N.USER_ID = U.USER_ID LEFT JOIN ( " +
            "SELECT NOTICE_SEQ, IMAGE_PATH, ROW_NUMBER() OVER (PARTITION BY NOTICE_SEQ ORDER BY IMAGE_SEQ) AS rn FROM myDB.NOTICE_IMAGE) I " +
            "ON N.NOTICE_SEQ = I.NOTICE_SEQ AND I.rn = 1 " +
            "WHERE N.USER_ID = :userId " + // 사용자 아이디를 명명된 매개변수로 추가
            "ORDER BY N.NOTICE_YN DESC, N.NOTICE_SEQ DESC", nativeQuery = true)
    List<NoticeSQLEntity> getUserNoticeListUsingSQL(@Param("userId") String userId);

    @Query(value = "SELECT N.NOTICE_SEQ, N.TITLE, N.NOTICE_YN, N.READ_CNT, N.USER_ID, N.REG_ID, N.REG_DT, N.CHG_ID, N.CHG_DT, " +
            "U.USER_NAME, U.PROFILE_PATH, I.IMAGE_PATH " +
            "FROM myDB.NOTICE N JOIN myDB.USER_INFO U ON N.USER_ID = U.USER_ID LEFT JOIN ( " +
            "SELECT NOTICE_SEQ, IMAGE_PATH, ROW_NUMBER() OVER (PARTITION BY NOTICE_SEQ ORDER BY IMAGE_SEQ) AS rn FROM myDB.NOTICE_IMAGE) I " +
            "ON N.NOTICE_SEQ = I.NOTICE_SEQ AND I.rn = 1 " +
            "WHERE N.USER_ID IN (:userIds) " +
            "ORDER BY N.NOTICE_YN DESC, N.NOTICE_SEQ DESC", nativeQuery = true)
    List<NoticeSQLEntity> getNoticeListByFollowedUsers(@Param("userIds") List<String> userIds);

}
