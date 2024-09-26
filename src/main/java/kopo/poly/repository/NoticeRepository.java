package kopo.poly.repository;

import kopo.poly.repository.entity.NoticeEntity;
import kopo.poly.repository.entity.NoticeSQLEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {

    /**
     * 공지사항 리스트
     *
     * @param noticeSeq 공지사항 PK
     */
    NoticeEntity findByNoticeSeq(Long noticeSeq);

    /**
     * 공지사항 상세 보기할 때, 조회수 증가하기
     *
     * @param noticeSeq 공지사항 PK
     */
    @Modifying(clearAutomatically = true) // Entity의 값을 날려버리고 새로운 값으로 변경함
    @Query(value = "UPDATE NOTICE A SET A.READ_CNT = IFNULL(A.READ_CNT, 0) + 1 WHERE A.NOTICE_SEQ = ?1",
            nativeQuery = true) // NativeQuery를 사용하는 경우 반드시 Modifying 사용
    int updateReadCnt(Long noticeSeq);

    /**
     * 특정 userId 기준 게시글 개수 조회
     */
    long countByUserId(String userId);
}
