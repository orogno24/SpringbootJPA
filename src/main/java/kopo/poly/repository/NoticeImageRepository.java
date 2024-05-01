package kopo.poly.repository;

import kopo.poly.repository.entity.NoticeImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeImageRepository extends JpaRepository<NoticeImageEntity, Long> {

    /**
     * 특정 noticeSeq에 대한 모든 이미지 찾기
     *
     * @param noticeSeq 공지사항 PK
     */
    List<NoticeImageEntity> findByNoticeSeq(Long noticeSeq);

}
