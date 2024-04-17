package kopo.poly.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.dto.ApiDTO;
import kopo.poly.dto.BookmarkDTO;
import kopo.poly.dto.EventDTO;
import kopo.poly.repository.BookmarkRepository;
import kopo.poly.repository.EventRespository;
import kopo.poly.repository.entity.BookmarkEntity;
import kopo.poly.repository.entity.EventEntity;
import kopo.poly.service.IEventService;
import kopo.poly.specification.EventSpecification;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.DateUtil;
import kopo.poly.util.NetworkUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventService implements IEventService  {

    private final EventRespository eventRepository;
    private final BookmarkRepository bookmarkRepository;

    @Value("${data.api.key}")
    private String apiKey;

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
    public ApiDTO getApiInfo(String uniqueIdentifier) throws Exception {

        String apiParam = apiKey + "/" + "json" + "/" + "culturalEventInfo" + "/" + "1" + "/" + "500" + "/";
        String json = NetworkUtil.get(IEventService.apiURL + apiParam);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> rMap = objectMapper.readValue(json, LinkedHashMap.class);
        Map<String, Object> culturalEventInfo = (Map<String, Object>) rMap.get("culturalEventInfo");
        List<Map<String, Object>> rContent = (List<Map<String, Object>>) culturalEventInfo.get("row");

        log.info("Unique Identifier: " + uniqueIdentifier);

        if (rContent != null) {
            for (Map<String, Object> content : rContent) {
                // 조건에 맞는 이벤트 정보 필터링
                if (content.get("MAIN_IMG").equals(uniqueIdentifier))  {

                    ApiDTO rDTO = objectMapper.convertValue(content, ApiDTO.class);

                    return rDTO;
                }
            }
        }

        log.info("No matching event found.");
        return ApiDTO.builder().build();
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

    @Override
    public List<ApiDTO> getList(ApiDTO pDTO) throws JsonProcessingException {

        log.info(this.getClass().getName() + ".getList Start!");

        String apiParam = apiKey + "/" + "json" + "/" + "culturalEventInfo" + "/" + "1" + "/" + "500" + "/";
        String json = NetworkUtil.get(IEventService.apiURL + apiParam);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> rMap = objectMapper.readValue(json, LinkedHashMap.class);

        Map<String, Object> culturalEventInfo = (Map<String, Object>) rMap.get("culturalEventInfo");
        List<Map<String, Object>> rContent = (List<Map<String, Object>>) culturalEventInfo.get("row");

        // 날짜 및 시간을 처리할 수 있는 Formatter 정의
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

        // 조건에 맞는 데이터만 필터링
        Stream<ApiDTO> stream = rContent.stream().map(content -> objectMapper.convertValue(content, ApiDTO.class));

        // guName이 설정되어 있다면, 해당 값으로 필터링
        if (pDTO.guName() != null && !pDTO.guName().isEmpty()) {
            stream = stream.filter(e -> e.guName().equals(pDTO.guName()));
        }

        // codename이 설정되어 있다면, 해당 값으로 필터링
        if (pDTO.codename() != null && !pDTO.codename().isEmpty()) {
            stream = stream.filter(e -> e.codename().equals(pDTO.codename()));
        }

        // themeCode가 설정되어 있다면, 해당 값으로 필터링
        if (pDTO.themeCode() != null && !pDTO.themeCode().isEmpty()) {
            stream = stream.filter(e -> e.themeCode().equals(pDTO.themeCode()));
        }

        // isFree가 설정되어 있다면, 해당 값으로 필터링
        if (pDTO.isFree() != null && !pDTO.isFree().isEmpty()) {
            stream = stream.filter(e -> e.isFree().equals(pDTO.isFree()));
        }

        // startDate가 설정되어 있다면, 해당 날짜 이전의 이벤트는 제외
        if (pDTO.startDate() != null && !pDTO.startDate().isEmpty()) {
            LocalDate startDate = LocalDate.parse(pDTO.startDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            stream = stream.filter(e -> {
                LocalDateTime eventDateTime = LocalDateTime.parse(e.startDate(), formatter);
                return !eventDateTime.toLocalDate().isBefore(startDate);
            });
        }

        // endDate가 설정되어 있다면, 해당 날짜 이후의 이벤트는 제외
        if (pDTO.endDate() != null && !pDTO.endDate().isEmpty()) {
            LocalDate endDate = LocalDate.parse(pDTO.endDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            stream = stream.filter(e -> {
                LocalDateTime eventDateTime = LocalDateTime.parse(e.endDate(), formatter);
                return !eventDateTime.toLocalDate().isAfter(endDate);
            });
        }

        // 필터링된 결과를 리스트로 수집
        List<ApiDTO> pList = stream.collect(Collectors.toList());

        log.info(this.getClass().getName() + ".getList End!");

        return pList;
    }

    @Override
    public List<BookmarkDTO> getBookmarkSeq(BookmarkDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".getBookmarkSeq Start!");

        String userId = CmmUtil.nvl(pDTO.userId());

        List<BookmarkEntity> rList = bookmarkRepository.findAllByUserId(userId);

        List<BookmarkDTO> nList = new ObjectMapper().convertValue(rList,
                new TypeReference<List<BookmarkDTO>>() {
                });

        log.info(this.getClass().getName() + ".getBookmarkSeq End!");

        return nList;
    }

    @Override
    public List<BookmarkDTO> getBookmarkDateList(List<String> BookMarkSeqList) throws JsonProcessingException {
        // API 호출을 위한 파라미터 설정
        String apiParam = apiKey + "/" + "json" + "/" + "culturalEventInfo" + "/" + "1" + "/" + "500" + "/";
        String json = NetworkUtil.get(IEventService.apiURL + apiParam);

        // JSON 응답을 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> rMap = objectMapper.readValue(json, LinkedHashMap.class);

        // "culturalEventInfo" 객체 내 "row" 배열 추출
        Map<String, Object> culturalEventInfo = (Map<String, Object>) rMap.get("culturalEventInfo");
        List<Map<String, Object>> rContent = (List<Map<String, Object>>) culturalEventInfo.get("row");

        // 필터링된 이벤트 정보를 담을 리스트
        List<BookmarkDTO> filteredEvents = new ArrayList<>();
        for (Map<String, Object> event : rContent) {
            String mainImgUrl = (String) event.get("MAIN_IMG");
            String extractedId = extractImageId(mainImgUrl);

            // 북마크된 이벤트 ID와 일치하는 이벤트만 선택
            if (BookMarkSeqList.contains(extractedId)) {
                String startDate = (String) event.get("STRTDATE");
                String endDate = (String) event.get("END_DATE");

                // 날짜 데이터 가공
                startDate = startDate.split(" ")[0];
                endDate = endDate.split(" ")[0];

                LocalDate originalEndDate = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
                endDate = originalEndDate.plusDays(1).toString();

                BookmarkDTO dto = BookmarkDTO.builder()
                        .eventTitle(String.valueOf(event.get("TITLE")))
                        .startDate(startDate)
                        .endDate(endDate)
                        .nSeq(extractedId)
                        .build();

                filteredEvents.add(dto);
            }
        }

        return filteredEvents;
    }

    // 이미지 URL에서 파일 ID를 추출하는 메소드
    private String extractImageId(String url) {
        String[] parts = url.split("atchFileId=");
        return parts.length > 1 ? parts[1].split("&")[0] : "";
    }



    @Override
    public void insertBookmark(BookmarkDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".insertBookmark Start!");

        String userId = CmmUtil.nvl(pDTO.userId());
        String eventSeq = CmmUtil.nvl(pDTO.eventSeq());
        String eventTitle = CmmUtil.nvl(pDTO.eventTitle());
        String startDate = CmmUtil.nvl(pDTO.startDate());
        String endDate = CmmUtil.nvl(pDTO.endDate());

        startDate = startDate.split(" ")[0];
        endDate = endDate.split(" ")[0];

        LocalDate originalEndDate = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
        endDate = originalEndDate.plusDays(1).toString();

        log.info("userId : " + userId);
        log.info("eventSeq : " + eventSeq);
        log.info("eventTitle : " + eventTitle);
        log.info("startDate : " + startDate);
        log.info("endDate : " + endDate);

        BookmarkEntity pEntity = BookmarkEntity.builder()
                .userId(userId)
                .regDt(DateUtil.getDateTime("yyyy-MM-dd hh:mm:ss"))
                .eventSeq(eventSeq)
                .eventTitle(eventTitle)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        // 공지사항 저장하기
        bookmarkRepository.save(pEntity);

        log.info(this.getClass().getName() + ".insertBookmark End!");

    }

    @Override
    public void removeBookmark(BookmarkDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".removeBookmark Start!");

        String userId = pDTO.userId();
        String eventSeq = pDTO.eventSeq();

        log.info("userId : " + userId);
        log.info("eventSeq : " + eventSeq);

        Optional<BookmarkEntity> rEntity = bookmarkRepository.findByUserIdAndEventSeq(userId, eventSeq);

        Long bookmarkSeq = rEntity.get().getBookmarkSeq();

        log.info("bookmarkSeq : " + bookmarkSeq);

        bookmarkRepository.deleteById(bookmarkSeq);

        log.info(this.getClass().getName() + ".removeBookmark End!");

    }

    @Override
    public BookmarkDTO getBookmarkExists(BookmarkDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".getBookmarkExists Start!");

        BookmarkDTO rDTO;

        String userId= CmmUtil.nvl(pDTO.userId());
        String eventSeq= CmmUtil.nvl(pDTO.eventSeq());

        log.info("userId : " + userId);
        log.info("eventSeq : " + eventSeq);

        Optional<BookmarkEntity> rEntity = bookmarkRepository.findByUserIdAndEventSeq(userId, eventSeq);

        if (rEntity.isPresent()) {
            rDTO = BookmarkDTO.builder().existsYn("Y").build();
        } else {
            rDTO = BookmarkDTO.builder().existsYn("N").build();
        }

        log.info(this.getClass().getName() + ".getBookmarkExists End!");

        return rDTO;

    }


//    @Transactional
//    @Override
//    public ApiDTO parseUniqueIdentifierToDTO(String uniqueIdentifier) {
//
//        log.info(this.getClass().getName() + ".parseUniqueIdentifierToDTO Start!");
//
//        log.info("Unique Identifier: " + uniqueIdentifier);
//
//        // 예시 구현: uniqueIdentifier는 'title_startDate_place' 형식으로 구성되어 있다고 가정
//        String[] parts = uniqueIdentifier.split("cSeq");
//        if (parts.length != 3) {
//            // 식별자 형식이 올바르지 않은 경우, 빈 DTO 반환 또는 예외 처리
//            return ApiDTO.builder().build();
//        }
//
//        String title = parts[0];
//        String startDate = parts[1];
//        String place = parts[2];
//
//        // EventDTO 객체 생성 및 필드 값 설정
//        ApiDTO pDTO = ApiDTO.builder()
//                .title(title)
//                .startDate(startDate) // 이 필드는 예시이며, 실제 DTO 필드와 일치해야 합니다.
//                .place(place) // 이 필드는 예시이며, 실제 DTO 필드와 일치해야 합니다.
//                .build();
//
//        log.info("title : " + title);
//        log.info("startDate : " + startDate);
//        log.info("place : " + place);
//
//        log.info(this.getClass().getName() + ".parseUniqueIdentifierToDTO End!");
//
//        return pDTO;
//    }


}
