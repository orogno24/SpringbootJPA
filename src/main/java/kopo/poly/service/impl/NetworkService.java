package kopo.poly.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import kopo.poly.dto.NetworkDTO;
import kopo.poly.dto.NoticeDTO;
import kopo.poly.repository.NetworkRepository;
import kopo.poly.repository.NetworkSQLRepository;
import kopo.poly.repository.entity.CommentEntity;
import kopo.poly.repository.entity.NetworkEntity;
import kopo.poly.repository.entity.NetworkSQLEntity;
import kopo.poly.repository.entity.NoticeSQLEntity;
import kopo.poly.service.INetworkService;
import kopo.poly.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class NetworkService implements INetworkService {

    private final NetworkRepository networkRepository;
    private final NetworkSQLRepository networkSQLRepository;

    @Override
    public void insertNetWorkInfo(NetworkDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".insertNetWorkInfo Start!");

        String type = pDTO.eventSeq().matches(".*[a-zA-Z]+.*") ? "EVENT" : "CULTURE";

        NetworkEntity pEntity = NetworkEntity.builder()
                .userId(pDTO.userId())
                .name(pDTO.name())
                .contents(pDTO.contents())
                .startDate(pDTO.startDate())
                .endDate(pDTO.endDate())
                .eventSeq(pDTO.eventSeq())
                .eventName(pDTO.eventName())
                .imagePath(pDTO.imagePath())
                .regDt(DateUtil.getDateTime("yyyy-MM-dd hh:mm:ss"))
                .type(type)
                .build();

        networkRepository.save(pEntity);

        log.info(this.getClass().getName() + ".insertNetWorkInfo End!");

    }

    @Override
    public List<NetworkDTO> getNetworkList(String type) throws Exception {

        log.info(this.getClass().getName() + ".getNetworkList Start!");

        List<NetworkSQLEntity> rList = networkSQLRepository.getNetworkList(type);

        log.info("rList : " + rList);

        List<NetworkDTO> nList = new ObjectMapper().convertValue(rList,
                new TypeReference<List<NetworkDTO>>() {
                });

        log.info(this.getClass().getName() + ".getNetworkList End!");

        return nList;
    }

    @Override
    public NetworkDTO getNetworkInfo(String networkSeq) throws Exception {
        log.info(this.getClass().getName() + ".getNetworkList Start!");

        NetworkSQLEntity rEntity = networkSQLRepository.getNetworkInfo(networkSeq);

        NetworkDTO rDTO = new ObjectMapper().convertValue(rEntity, NetworkDTO.class);

        log.info(this.getClass().getName() + ".getNetworkList End!");

        return rDTO;
    }
}
