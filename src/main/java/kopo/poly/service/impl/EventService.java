package kopo.poly.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kopo.poly.dto.CommentDTO;
import kopo.poly.dto.EventDTO;
import kopo.poly.dto.NoticeDTO;
import kopo.poly.repository.CommentRepository;
import kopo.poly.repository.EventRespository;
import kopo.poly.repository.NoticeRepository;
import kopo.poly.repository.entity.*;
import kopo.poly.service.IEventService;
import kopo.poly.service.INoticeJoinService;
import kopo.poly.specification.EventSpecification;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventService implements IEventService  {

    private final EventRespository eventRepository;

    @Override
    public List<EventDTO> getEventList() {
        log.info(this.getClass().getName() + ".getEventList Start!");

        // 공지사항 전체 리스트 조회하기
        List<EventEntity> rList = eventRepository.findAllByOrderByEventSeqDesc();

        // 엔티티의 값들을 DTO에 맞게 넣어주기
        List<EventDTO> nList = new ObjectMapper().convertValue(rList,
                new TypeReference<List<EventDTO>>() {
                });

        log.info(this.getClass().getName() + ".getEventList End!");

        return nList;
    }

    @Transactional
    @Override
    public EventDTO getEventInfo(EventDTO pDTO, boolean type) throws Exception {

        log.info(this.getClass().getName() + ".getEventInfo Start!");

        // 공지사항 상세내역 가져오기
        EventEntity rEntity = eventRepository.findByEventSeq(pDTO.eventSeq());

        // 엔티티의 값들을 DTO에 맞게 넣어주기
        EventDTO rDTO = new ObjectMapper().convertValue(rEntity, EventDTO.class);

        log.info(this.getClass().getName() + ".getEventInfo End!");

        return rDTO;
    }

    @Override
    public List<EventDTO> getEventListSearch(EventDTO pDTO) {
        log.info(this.getClass().getName() + ".getEventListSearch Start!");

        Specification<EventEntity> spec = Specification.where(EventSpecification.eventPlace(pDTO.eventPlace()))
                .and(EventSpecification.eventSort(pDTO.eventSort()))
                .and(EventSpecification.eventDate(pDTO.eventDate()));

        Sort sort = Sort.by(Sort.Direction.ASC, "eventSeq");
        List<EventEntity> rList = eventRepository.findAll(spec, sort);

        // 엔티티의 값들을 DTO에 맞게 넣어주기
        List<EventDTO> nList = new ObjectMapper().convertValue(rList,
                new TypeReference<List<EventDTO>>() {
                });

        log.info(this.getClass().getName() + ".getEventListSearch End!");

        return nList;
    }
}
