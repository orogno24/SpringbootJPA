package kopo.poly.service.impl;

import kopo.poly.dto.NetworkDTO;
import kopo.poly.repository.NetworkRepository;
import kopo.poly.repository.entity.CommentEntity;
import kopo.poly.repository.entity.NetworkEntity;
import kopo.poly.service.INetworkService;
import kopo.poly.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class NetworkService implements INetworkService {

    private final NetworkRepository networkRepository;

    @Override
    public void insertNetWorkInfo(NetworkDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".insertNetWorkInfo Start!");

        NetworkEntity pEntity = NetworkEntity.builder()
                .userId(pDTO.userId())
                .name(pDTO.name())
                .contents(pDTO.contents())
                .dateTime(pDTO.dateTime())
                .maxParticipants(pDTO.maxParticipants())
                .eventSeq(pDTO.eventSeq())
                .cultureSeq(pDTO.cultureSeq())
                .build();

        networkRepository.save(pEntity);

        log.info(this.getClass().getName() + ".insertNetWorkInfo End!");

    }
}
