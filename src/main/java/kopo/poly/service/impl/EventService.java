package kopo.poly.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.dto.ApiDTO;
import kopo.poly.dto.BookmarkDTO;
import kopo.poly.dto.RedisDTO;
import kopo.poly.redis.IRedisMapper;
import kopo.poly.repository.BookmarkRepository;
import kopo.poly.repository.EventRespository;
import kopo.poly.repository.entity.BookmarkEntity;
import kopo.poly.service.IEventService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.DateUtil;
import kopo.poly.util.ExtractImageUtil;
import kopo.poly.util.NetworkUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
    private final IRedisMapper redisMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${data.api.key}")
    private String apiKey;

    /**
     * 조건에 맞는 문화행사정보 필터링
     *
     * @param uniqueIdentifier 필터링 기준 키
     * @return 필터링된 문화행사
     */
    @Override
    public ApiDTO getApiInfo(RedisDTO redisDTO, String uniqueIdentifier) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> rContent = objectMapper.readValue(redisDTO.contents(), new TypeReference<List<Map<String, Object>>>() {});

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

    /**
     * 조건에 맞는 문화행사정보 리스트 필터링
     *
     * @param pDTO 필터링 조건
     * @return 필터링된 문화행사 리스트
     */
    @Override
    public List<ApiDTO> getList(RedisDTO redisDTO, ApiDTO pDTO) throws JsonProcessingException {

        log.info(this.getClass().getName() + ".getList Start!");

        ObjectMapper objectMapper = new ObjectMapper();

        // RedisDTO의 contents를 List<Map<String, Object>>로 변환
        List<Map<String, Object>> rContent = objectMapper.readValue(redisDTO.contents(), new TypeReference<List<Map<String, Object>>>() {});

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

    /**
     * RedisDB를 활용한 문화행사 리스트 조회
     *
     *  @param colNm 테이블 이름
     *  @return 문화행사 리스트
     */
    @Override
    public RedisDTO getCulturalEvents(String colNm) throws Exception {

        List<Map<String, Object>> rContent;

        // 저장 결과
        RedisDTO rDTO = null;

        if (!redisMapper.getExistKey(colNm)) {

            // API 호출을 위한 파라미터 설정
            String apiParam = apiKey + "/" + "json" + "/" + "culturalEventInfo" + "/" + "1" + "/" + "900" + "/";
            String json = NetworkUtil.get(IEventService.apiURL + apiParam);

            // JSON 응답을 객체로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> rMap = objectMapper.readValue(json, LinkedHashMap.class);

            // "culturalEventInfo" 객체 내 "row" 배열 추출
            Map<String, Object> culturalEventInfo = (Map<String, Object>) rMap.get("culturalEventInfo");
            rContent = (List<Map<String, Object>>) culturalEventInfo.get("row");

            redisMapper.insertEventList(rContent, colNm);

            String contents = objectMapper.writeValueAsString(rContent);
            rDTO = RedisDTO.builder()
                    .contents(contents)
                    .build();

        } else {

            rDTO = redisMapper.getEventList(colNm);

        }

        return rDTO;
    }

    /**
     * 오늘의 문화행사 리스트 검색
     *
     *  @param redisDTO 문화행사 리스트
     *  @param pDTO 필터링 조건
     *  @return 오늘의 문화행사 리스트
     */
    @Override
    public List<ApiDTO> getTodayEventList(RedisDTO redisDTO, ApiDTO pDTO) throws JsonProcessingException {

        log.info(this.getClass().getName() + ".getTodayEventList Start!");

        ObjectMapper objectMapper = new ObjectMapper();

        // RedisDTO의 contents를 List<Map<String, Object>>로 변환
        List<Map<String, Object>> rContent = objectMapper.readValue(redisDTO.contents(), new TypeReference<List<Map<String, Object>>>() {});

        // 날짜 및 시간을 처리할 수 있는 Formatter 정의
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

        // 조건에 맞는 데이터만 필터링
        Stream<ApiDTO> stream = rContent.stream().map(content -> objectMapper.convertValue(content, ApiDTO.class));

        log.info("pDTO.startDate : " + pDTO.startDate());
        log.info("pDTO.endDate : " + pDTO.endDate());

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
        List<ApiDTO> filteredList = stream.collect(Collectors.toList());
        log.info("FilteredList size: " + filteredList.size());

        // 리스트를 랜덤하게 섞음
        Collections.shuffle(filteredList);

        // 최대 3개의 요소만 가질 수 있도록 리스트를 재조정
        List<ApiDTO> pList = filteredList.stream().limit(3).collect(Collectors.toList());
        log.info("pList size: " + pList.size());

        log.info(this.getClass().getName() + ".getTodayEventList End!");

        return pList;

    }

    /**
     * 행사가 가장 많이 열리는 지역구 5개 추출 (그래프 용도)
     *
     *  @param redisDTO 문화행사 리스트
     *  @param pDTO 필터링 조건
     *  @return 행사가 가장 많이 열리는 지역구 리스트
     */
    public Map<String, Long> getEventCountList(RedisDTO redisDTO, ApiDTO pDTO) throws JsonProcessingException {
        log.info(this.getClass().getName() + ".getEventCountList Start!");

        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> rContent = objectMapper.readValue(redisDTO.contents(), new TypeReference<List<Map<String, Object>>>() {});

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

        LocalDate startDate = LocalDate.parse(pDTO.startDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate endDate = LocalDate.parse(pDTO.endDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        log.info("startDate : " + startDate);
        log.info("endDate : " + endDate);

        // 각 구별로 행사 개수 세기
        Map<String, Long> districtEventCount = rContent.stream()
                .filter(content -> {
                    String startDateStr = (String) content.get("STRTDATE");
                    String endDateStr = (String) content.get("END_DATE");
                    LocalDateTime eventStartDate = LocalDateTime.parse(startDateStr, formatter);
                    LocalDateTime eventEndDate = LocalDateTime.parse(endDateStr, formatter);
                    boolean isInDateRange = !eventStartDate.toLocalDate().isBefore(startDate) && !eventEndDate.toLocalDate().isAfter(endDate);

//                    if (isInDateRange) {
//                        log.info("Event " + content.get("EVENTID") + ": Start Date = " + eventStartDate + ", End Date = " + eventEndDate + ", In Date Range: " + isInDateRange);
//                    }

                    return isInDateRange;
                })
                .collect(Collectors.groupingBy(content -> content.get("GUNAME").toString(), Collectors.counting()));

        log.info("districtEventCount : " + districtEventCount);

        // 상위 5개 구 추출
        Map<String, Long> top5Districts = districtEventCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        log.info("top5Districts : " + top5Districts);

        log.info(this.getClass().getName() + ".getEventCountList End!");

        return top5Districts;
    }

    /**
     * 이벤트 유형 개수 추출 (그래프 용도)
     *
     *  @param redisDTO 문화행사 리스트
     *  @param pDTO 필터링 조건
     *  @return 이벤트 유형 리스트
     */
    public Map<String, Long> getEventTypeCountList(RedisDTO redisDTO, ApiDTO pDTO) throws JsonProcessingException {
        log.info(this.getClass().getName() + ".getEventTypeCountList Start!");

        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> rContent = objectMapper.readValue(redisDTO.contents(), new TypeReference<List<Map<String, Object>>>() {});

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

        LocalDate startDate = LocalDate.parse(pDTO.startDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate endDate = LocalDate.parse(pDTO.endDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        log.info("startDate : " + startDate);
        log.info("endDate : " + endDate);

        // 각 행사 유형별로 행사 개수 세기
        Map<String, Long> eventTypeCount = rContent.stream()
                .filter(content -> {
                    String startDateStr = (String) content.get("STRTDATE");
                    String endDateStr = (String) content.get("END_DATE");
                    LocalDateTime eventStartDate = LocalDateTime.parse(startDateStr, formatter);
                    LocalDateTime eventEndDate = LocalDateTime.parse(endDateStr, formatter);
                    return !eventStartDate.toLocalDate().isBefore(startDate) && !eventEndDate.toLocalDate().isAfter(endDate);
                })
                .collect(Collectors.groupingBy(content -> content.get("CODENAME").toString(), Collectors.counting()));

        log.info("eventTypeCount : " + eventTypeCount);

        // 상위 6개 행사 유형 추출
        Map<String, Long> top6EventTypes = eventTypeCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        log.info("top6EventTypes : " + top6EventTypes);

        log.info(this.getClass().getName() + ".getEventTypeCountList End!");

        return top6EventTypes;
    }

    /**
     * 유저 북마크에서 BookmarkSeq만 추출 후 List에 담기
     * @param pDTO 유저아이디
     * @return  북마크의 BookmarkSeq 리스트
     */
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

    /**
     * BookmarkSeq List를 기준으로 전체 데이터 필터링
     * @param BookMarkSeqList BookmarkSeq 목록
     * @return BookmarkSeq 기준 문화행사 리스트
     */
    @Override
    public List<BookmarkDTO> getBookmarkDateList(RedisDTO redisDTO, List<String> BookMarkSeqList) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> rContent = objectMapper.readValue(redisDTO.contents(), new TypeReference<List<Map<String, Object>>>() {});

        // 필터링된 이벤트 정보를 담을 리스트
        List<BookmarkDTO> filteredEvents = new ArrayList<>();

        for (Map<String, Object> event : rContent) {
            String mainImgUrl = (String) event.get("MAIN_IMG");
            String extractedId = ExtractImageUtil.extractImageId(mainImgUrl);

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

    /**
     * 북마크 추가하기
     * @param pDTO 북마크 정보
     */
    @Override
    public void insertBookmark(BookmarkDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".insertBookmark Start!");

        String userId = CmmUtil.nvl(pDTO.userId());
        String eventSeq = CmmUtil.nvl(pDTO.eventSeq());
//        String eventTitle = CmmUtil.nvl(pDTO.eventTitle());
//        String startDate = CmmUtil.nvl(pDTO.startDate());
//        String endDate = CmmUtil.nvl(pDTO.endDate());
//
//        startDate = startDate.split(" ")[0];
//        endDate = endDate.split(" ")[0];

//        LocalDate originalEndDate = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
//        endDate = originalEndDate.plusDays(1).toString();

        log.info("userId : " + userId);
        log.info("eventSeq : " + eventSeq);
//        log.info("eventTitle : " + eventTitle);
//        log.info("startDate : " + startDate);
//        log.info("endDate : " + endDate);

        BookmarkEntity pEntity = BookmarkEntity.builder()
                .userId(userId)
                .regDt(DateUtil.getDateTime("yyyy-MM-dd hh:mm:ss"))
                .eventSeq(eventSeq)
                .build();

        // 공지사항 저장하기
        bookmarkRepository.save(pEntity);

        log.info(this.getClass().getName() + ".insertBookmark End!");

    }

    /**
     * 북마크 해제하기
     * @param pDTO 북마크 정보
     */
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

    /**
     * 북마크가 존재하는지 조회
     * @param pDTO 북마크 정보
     */
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
