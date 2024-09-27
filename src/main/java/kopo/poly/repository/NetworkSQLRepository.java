package kopo.poly.repository;

import kopo.poly.repository.entity.NetworkSQLEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkSQLRepository extends JpaRepository<NetworkSQLEntity, Long> {

    @Query(value = "SELECT n.NETWORK_SEQ, n.USER_ID, n.NAME, n.CONTENTS, n.START_DATE, n.END_DATE, n.USER_COUNT, n.EVENT_SEQ, n.EVENT_NAME, n.IMAGE_PATH, n.REG_DT, n.TYPE, " +
            "u.USER_NAME, u.PROFILE_PATH " +
            "FROM myDB.NETWORK_INFO n " +
            "JOIN myDB.USER_INFO u ON n.USER_ID = u.USER_ID " +
            "WHERE n.TYPE = :type " +
            "ORDER BY n.NETWORK_SEQ DESC", nativeQuery = true)
    List<NetworkSQLEntity> getNetworkList(@Param("type") String type);

    @Query(value = "SELECT n.NETWORK_SEQ, n.USER_ID, n.NAME, n.CONTENTS, n.START_DATE, n.END_DATE, n.USER_COUNT, n.EVENT_SEQ, n.EVENT_NAME, n.IMAGE_PATH, n.REG_DT, n.TYPE, " +
            "u.USER_NAME, u.PROFILE_PATH " +
            "FROM myDB.NETWORK_INFO n " +
            "JOIN myDB.USER_INFO u ON n.USER_ID = u.USER_ID " +
            "WHERE n.NETWORK_SEQ = :networkSeq ", nativeQuery = true)
    NetworkSQLEntity getNetworkInfo(@Param("networkSeq") String type);


}