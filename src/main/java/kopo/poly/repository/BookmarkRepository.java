package kopo.poly.repository;

import kopo.poly.repository.entity.BookmarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<BookmarkEntity, Long> {

    Optional<BookmarkEntity> findByUserIdAndEventSeq(String userId, String eventSeq);

    List<BookmarkEntity> findAllByUserId(String userId);

}