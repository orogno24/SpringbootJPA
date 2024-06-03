package kopo.poly.repository;

import kopo.poly.repository.entity.CommentEntity;
import kopo.poly.repository.entity.CommentPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, CommentPK> {

    @Transactional(readOnly = true)
    @Query(value = "SELECT COALESCE(MAX(COMMENT_SEQ), 0)+1 FROM NOTICE_COMMENT WHERE NOTICE_SEQ = ?1",
            nativeQuery = true)
    Long getNextCommentSeq(Long noticeSeq);

}
