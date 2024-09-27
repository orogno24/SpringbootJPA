package kopo.poly.repository;

import jakarta.persistence.LockModeType;
import kopo.poly.dto.NetworkDTO;
import kopo.poly.repository.entity.NetworkEntity;
import kopo.poly.repository.entity.NoticeSQLEntity;
import kopo.poly.repository.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NetworkRepository extends JpaRepository<NetworkEntity, Long> {

    long countByEventSeq(String eventSeq);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM NetworkEntity n WHERE n.networkSeq = :networkSeq")
    Optional<NetworkEntity> findNetworkWithLock(@Param("networkSeq") Long networkSeq);

}