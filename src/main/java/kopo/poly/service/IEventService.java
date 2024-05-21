package kopo.poly.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kopo.poly.dto.ApiDTO;
import kopo.poly.dto.BookmarkDTO;
import kopo.poly.dto.EventDTO;
import kopo.poly.dto.NoticeDTO;

import java.util.List;

public interface IEventService {

    String apiURL = "http://openapi.seoul.go.kr:8088/";

    /**
     * 문화행사 전체 가져오기
     */
    List<EventDTO> getEventList();



    /**
     * 문화행사 상세 정보가져오기
     *
     * @param pDTO 문화행사 상세 가져오기 위한 정보
     * @param type 조회수 증가여부(true : 증가, false : 증가안함
     */
    EventDTO getEventInfo(EventDTO pDTO, boolean type) throws Exception;
    ApiDTO getApiInfo(String uniqueIdentifier) throws Exception;

    /**
     * 문화행사 검색
     */
    List<EventDTO> getEventListSearch(EventDTO pDTO);

    List<ApiDTO> getList(ApiDTO pDTO) throws JsonProcessingException;

    /**
     * 오늘의 문화행사 검색
     */
    List<ApiDTO> getTodayEventList(ApiDTO pDTO) throws JsonProcessingException;

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
