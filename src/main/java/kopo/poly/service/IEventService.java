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

    List<ApiDTO> getBookmarkList(List<String> bookmarkedIdentifiersO) throws JsonProcessingException;

    List<BookmarkDTO> getBookmarkDate(BookmarkDTO pDTO) throws Exception;

    void insertBookmark(BookmarkDTO pDTO) throws Exception;

    void removeBookmark(BookmarkDTO pDTO) throws Exception;

    BookmarkDTO getBookmarkExists(BookmarkDTO pDTO) throws Exception;

    //    ApiDTO parseUniqueIdentifierToDTO(String uniqueIdentifier) throws Exception;

}
