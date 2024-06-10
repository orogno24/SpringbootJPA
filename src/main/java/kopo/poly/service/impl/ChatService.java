package kopo.poly.service.impl;

import kopo.poly.repository.ChatRepository;
import kopo.poly.repository.entity.ChatEntitiy;
import kopo.poly.service.IChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService implements IChatService {

    private final ChatRepository chatRepository;

    @Override
    public boolean findByRoomName(String roomName) throws Exception {

        log.info(this.getClass().getName() + ".findByRoomName Start!");

        Optional<ChatEntitiy> rEntity = chatRepository.findByRoomName(roomName);

        log.info(this.getClass().getName() + ".findByRoomName End!");

        return rEntity.isPresent();
    }

    @Override
    public void insertRoomName(String roomName, String userId) throws Exception {

        log.info(this.getClass().getName() + ".insertRoomName Start!");

        ChatEntitiy pEntity = ChatEntitiy.builder()
                .roomName(roomName)
                .userId(userId)
                .build();

        chatRepository.save(pEntity);

        log.info(this.getClass().getName() + ".insertRoomName End!");
    }

    @Override
    @Transactional
    public void deleteRoomName(String roomName) throws Exception {

        log.info(this.getClass().getName() + ".deleteRoomName Start!");

        chatRepository.deleteByRoomName(roomName);

        log.info(this.getClass().getName() + ".deleteRoomName End!");

    }

    @Override
    public String getRoomOwner(String roomName) throws Exception {

        log.info(this.getClass().getName() + ".getRoomOwner Start!");

        Optional<ChatEntitiy> rEntity = chatRepository.findByRoomName(roomName);

        log.info(this.getClass().getName() + ".getRoomOwner End!");

        if (rEntity.isPresent()) {
            return rEntity.get().getUserId();
        } else {
            throw new Exception("Room not found: " + roomName);
        }
    }


}

