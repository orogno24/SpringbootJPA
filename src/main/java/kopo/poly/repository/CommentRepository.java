package kopo.poly.repository;

import kopo.poly.repository.entity.CommentEntity;
import kopo.poly.repository.entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.xml.stream.events.Comment;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {



}
