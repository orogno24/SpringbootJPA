package kopo.poly.repository;

import kopo.poly.repository.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRespository extends JpaRepository<EventEntity, Long> {

    /**
     * 문화행사 리스트
     */
    List<EventEntity> findAllByOrderByEventSeqDesc();

    /**
     * 문화행사 리스트
     *
     * @param eventSeq 문화행사 PK
     */
    EventEntity findByEventSeq(Long eventSeq);

}
