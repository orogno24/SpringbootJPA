package kopo.poly.repository;

import kopo.poly.repository.entity.ChatEntitiy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntitiy, Long> {

    Optional<ChatEntitiy> findByRoomName(String roomName);

    void deleteByRoomName(String roomName);
}
