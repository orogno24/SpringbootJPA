package kopo.poly.repository;

import kopo.poly.repository.entity.EventEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRespository extends JpaRepository<EventEntity, Long>, JpaSpecificationExecutor<EventEntity> {

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

    /**
     * 문화행사 검색
     */
    List<EventEntity> findAllByEventWhereAndEventSortAndEventDate(String eventWhere, String eventSort, String eventDate);


}
