package kopo.poly.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kopo.poly.dto.ApiDTO;
import kopo.poly.dto.BookmarkDTO;
import kopo.poly.dto.EventDTO;
import kopo.poly.dto.NoticeDTO;

import java.util.List;
import java.util.Map;

public interface IEventService {

    String apiURL = "http://openapi.seoul.go.kr:8088/";


    ApiDTO getApiInfo(String uniqueIdentifier) throws Exception;

    List<ApiDTO> getList(ApiDTO pDTO) throws JsonProcessingException;

    /**
     * RedisDB를 활용한 문화행사 리스트 조회
     */
    List<Map<String, Object>> getCulturalEvents(String colNm) throws Exception;

    /**
     * 오늘의 문화행사 검색
     */
    List<ApiDTO> getTodayEventList(List<Map<String, Object>> rContent, ApiDTO pDTO) throws JsonProcessingException;

    /**
     * 행사가 가장 많이 열리는 지역구 5개 추출(그래프 용도)
     */
    Map<String, Long> getEventCountList(List<Map<String, Object>> rContent, ApiDTO pDTO) throws JsonProcessingException;

    /**
     * 이벤트 유형 개수 추출
     */
    Map<String, Long> getEventTypeCountList(List<Map<String, Object>> rContent, ApiDTO pDTO) throws JsonProcessingException;

    /**
     * 유저가 북마크한 데이터중에서 BookmarkSeq만 추출 후 List에 담기
     * @param pDTO 유저아이디
     */
    List<BookmarkDTO> getBookmarkSeq(BookmarkDTO pDTO) throws Exception;

    /**
     * BookmarkSeq List를 기준으로 전체 데이터 필터링
     * @param rList BookmarkSeq 목록
     */
    List<BookmarkDTO> getBookmarkDateList(List<String> rList) throws JsonProcessingException;

    void insertBookmark(BookmarkDTO pDTO) throws Exception;

    void removeBookmark(BookmarkDTO pDTO) throws Exception;

    BookmarkDTO getBookmarkExists(BookmarkDTO pDTO) throws Exception;

    //    ApiDTO parseUniqueIdentifierToDTO(String uniqueIdentifier) throws Exception;

}
