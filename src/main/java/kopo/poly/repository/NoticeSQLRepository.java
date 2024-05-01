package kopo.poly.repository;

import kopo.poly.repository.entity.NoticeSQLEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

//    @Query(value = "SELECT A.NOTICE_SEQ, A.NOTICE_YN, A.TITLE, A.CONTENTS, A.USER_ID, A.READ_CNT, " +
//            "A.REG_ID, A.REG_DT, A.CHG_ID, A.CHG_DT, B.USER_NAME " +
//            "FROM NOTICE A INNER JOIN USER_INFO B ON A.USER_ID = B.USER_ID " +
//            "ORDER BY NOTICE_SEQ DESC",
//            nativeQuery = true)
//    List<NoticeSQLEntity> getNoticeListUsingSQL();

//    @Query(value = "SELECT N.NOTICE_SEQ, N.TITLE, N.NOTICE_YN, N.READ_CNT, N.USER_ID, N.REG_ID, N.REG_DT, N.CHG_ID, N.CHG_DT, " +
//            "U.USER_NAME, U.PROFILE_PATH, I.IMAGE_PATH " +
//            "FROM myDB.NOTICE N JOIN myDB.USER_INFO U ON N.USER_ID = U.USER_ID LEFT JOIN " +
//            "myDB.NOTICE_IMAGE I ON N.NOTICE_SEQ = I.NOTICE_SEQ " +
//            "ORDER BY N.NOTICE_YN DESC, N.NOTICE_SEQ DESC",
//            nativeQuery = true)
//    List<NoticeSQLEntity> getNoticeListUsingSQL();
}
