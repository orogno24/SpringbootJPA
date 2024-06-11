package kopo.poly.repository;

import kopo.poly.repository.entity.NetworkEntity;
import kopo.poly.repository.entity.NoticeSQLEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkRepository extends JpaRepository<NetworkEntity, Long> {


}