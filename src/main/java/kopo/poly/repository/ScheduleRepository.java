package kopo.poly.repository;

import kopo.poly.repository.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {

    Optional<ScheduleEntity> findByUserIdAndNetworkSeq(String userId, Long networkSeq);

    List<ScheduleEntity> findAllByUserId(String userId);

    List<ScheduleEntity> findAllByNetworkSeq(Long networkSeq);

}