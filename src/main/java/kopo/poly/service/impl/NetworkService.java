package kopo.poly.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import kopo.poly.dto.NetworkDTO;
import kopo.poly.dto.ScheduleDTO;
import kopo.poly.dto.UserFollowDTO;
import kopo.poly.dto.UserInfoDTO;
import kopo.poly.repository.NetworkRepository;
import kopo.poly.repository.NetworkSQLRepository;
import kopo.poly.repository.ScheduleRepository;
import kopo.poly.repository.entity.NetworkEntity;
import kopo.poly.repository.entity.NetworkSQLEntity;
import kopo.poly.repository.entity.ScheduleEntity;
import kopo.poly.service.INetworkService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class NetworkService implements INetworkService {

    private final NetworkRepository networkRepository;
    private final NetworkSQLRepository networkSQLRepository;
    private final ScheduleRepository scheduleRepository;

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
    public List<NetworkDTO> getAllNetworkList() throws Exception {

        log.info(this.getClass().getName() + ".getAllNetworkList Start!");

        List<NetworkEntity> rList = networkRepository.findAll();

        log.info("rList : " + rList);

        List<NetworkDTO> nList = new ObjectMapper().convertValue(rList,
                new TypeReference<List<NetworkDTO>>() {
                });

        log.info(this.getClass().getName() + ".getAllNetworkList End!");

        return nList;

    }

    @Override
    public NetworkDTO getNetworkInfo(String networkSeq) throws Exception {
        log.info(this.getClass().getName() + ".getNetworkInfo Start!");

        NetworkSQLEntity rEntity = networkSQLRepository.getNetworkInfo(networkSeq);

        NetworkDTO rDTO = new ObjectMapper().convertValue(rEntity, NetworkDTO.class);

        log.info(this.getClass().getName() + ".getNetworkInfo End!");

        return rDTO;
    }

    @Override
    public void networkDelete(NetworkDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".networkDelete Start!");

        Long networkSeq = pDTO.networkSeq();

        log.info("networkSeq : " + networkSeq);

        // 데이터 수정하기
        networkRepository.deleteById(networkSeq);

        log.info(this.getClass().getName() + ".networkDelete End!");

    }

    @Override
    public void insertBookmark(ScheduleDTO pDTO) throws Exception {
        String userId = CmmUtil.nvl(pDTO.userId());
        Long networkSeq = pDTO.networkSeq();

        log.info("userId : " + userId);
        log.info("networkSeq : " + networkSeq);

        ScheduleEntity pEntity = ScheduleEntity.builder()
                .userId(userId)
                .regDt(DateUtil.getDateTime("yyyy-MM-dd hh:mm:ss"))
                .networkSeq(networkSeq)
                .build();

        scheduleRepository.save(pEntity);

        log.info(this.getClass().getName() + ".insertBookmark End!");
    }

    @Override
    public void removeBookmark(ScheduleDTO pDTO) throws Exception {
        log.info(this.getClass().getName() + ".removeBookmark Start!");

        String userId = pDTO.userId();
        Long networkSeq = pDTO.networkSeq();

        log.info("userId : " + userId);
        log.info("networkSeq : " + networkSeq);

        Optional<ScheduleEntity> rEntity = scheduleRepository.findByUserIdAndNetworkSeq(userId, networkSeq);

        Long bookmarkSeq = rEntity.get().getScheduleSeq();

        log.info("bookmarkSeq : " + bookmarkSeq);

        scheduleRepository.deleteById(bookmarkSeq);

        log.info(this.getClass().getName() + ".removeBookmark End!");
    }

    @Override
    public ScheduleDTO getBookmarkExists(ScheduleDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".getBookmarkExists Start!");

        ScheduleDTO rDTO;

        String userId= CmmUtil.nvl(pDTO.userId());
        Long networkSeq = pDTO.networkSeq();

        log.info("userId : " + userId);
        log.info("networkSeq : " + networkSeq);

        Optional<ScheduleEntity> rEntity = scheduleRepository.findByUserIdAndNetworkSeq(userId, networkSeq);

        if (rEntity.isPresent()) {
            rDTO = ScheduleDTO.builder().existsYn("Y").build();
        } else {
            rDTO = ScheduleDTO.builder().existsYn("N").build();
        }

        log.info(this.getClass().getName() + ".getBookmarkExists End!");

        return rDTO;

    }

    @Override
    public List<NetworkDTO> getNetworkListByUserId(NetworkDTO pDTO) throws Exception {
        log.info(this.getClass().getName() + ".getNetworkListByUserId Start!");

        String userId = CmmUtil.nvl(pDTO.userId());

        List<NetworkEntity> rList = networkRepository.findAllByUserId(userId);

        List<NetworkDTO> nList = new ObjectMapper().convertValue(rList,
                new TypeReference<List<NetworkDTO>>() {
                });

        log.info(this.getClass().getName() + ".getNetworkListByUserId End!");

        return nList;
    }

    @Override
    public List<ScheduleDTO> getScheduleSeq(ScheduleDTO pDTO) throws Exception {
        log.info(this.getClass().getName() + ".getScheduleSeq Start!");

        String userId = CmmUtil.nvl(pDTO.userId());

        List<ScheduleEntity> rList = scheduleRepository.findAllByUserId(userId);

        List<ScheduleDTO> nList = new ObjectMapper().convertValue(rList,
                new TypeReference<List<ScheduleDTO>>() {
                });

        log.info(this.getClass().getName() + ".getScheduleSeq End!");

        return nList;
    }

    @Override
    public List<NetworkDTO> getNetworkDateList(List<NetworkDTO> networkList, List<Long> scheduleSeqList) throws JsonProcessingException {

        List<NetworkDTO> filteredList = networkList.stream()
                .filter(network -> scheduleSeqList.contains(network.networkSeq()))
                .map(network -> NetworkDTO.builder()
                        .name(network.name())
                        .startDate(network.startDate())
                        .endDate(network.endDate())
                        .networkSeq(network.networkSeq())
                        .eventSeq(network.eventSeq())
                        .build())
                .collect(Collectors.toList());
        return filteredList;
    }


}
