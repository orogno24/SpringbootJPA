package kopo.poly.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kopo.poly.dto.*;

import java.util.List;
import java.util.Map;

public interface IEventService {

    String apiURL = "http://openapi.seoul.go.kr:8088/";

    /**
     * 조건에 맞는 문화행사정보 필터링
     *
     * @param uniqueIdentifier 필터링 기준 키
     * @return 필터링된 문화행사
     */
    ApiDTO getApiInfo(RedisDTO redisDTO, String uniqueIdentifier) throws Exception;

    /**
     * 조건에 맞는 문화행사정보 리스트 필터링
     *
     * @param pDTO 필터링 조건
     * @return 필터링된 문화행사 리스트
     */
    List<ApiDTO> getList(RedisDTO redisDTO, ApiDTO pDTO) throws JsonProcessingException;

    /**
     * RedisDB를 활용한 문화행사 리스트 조회
     *
     *  @param colNm 테이블 이름
     *  @return 문화행사 리스트
     */
    RedisDTO getCulturalEvents(String colNm) throws Exception;

    /**
     * 오늘의 문화행사 리스트 검색
     *
     *  @param redisDTO 문화행사 리스트
     *  @param pDTO 필터링 조건
     *  @return 오늘의 문화행사 리스트
     */
    List<ApiDTO> getTodayEventList(RedisDTO redisDTO, ApiDTO pDTO) throws JsonProcessingException;

    /**
     * 행사가 가장 많이 열리는 지역구 5개 추출 (그래프 용도)
     *
     *  @param redisDTO 문화행사 리스트
     *  @param pDTO 필터링 조건
     *  @return 행사가 가장 많이 열리는 지역구 리스트
     */
    Map<String, Long> getEventCountList(RedisDTO redisDTO, ApiDTO pDTO) throws JsonProcessingException;

    /**
     * 이벤트 유형 개수 추출 (그래프 용도)
     *
     *  @param redisDTO 문화행사 리스트
     *  @param pDTO 필터링 조건
     *  @return 이벤트 유형 리스트
     */
    Map<String, Long> getEventTypeCountList(RedisDTO redisDTO, ApiDTO pDTO) throws JsonProcessingException;

    /**
     * 유저 북마크에서 BookmarkSeq만 추출 후 List에 담기
     * @param pDTO 유저아이디
     * @return  북마크의 BookmarkSeq 리스트
     */
    List<BookmarkDTO> getBookmarkSeq(BookmarkDTO pDTO) throws Exception;

    /**
     * BookmarkSeq List를 기준으로 전체 데이터 필터링
     * @param BookMarkSeqList BookmarkSeq 목록
     * @return BookmarkSeq 기준 문화행사 리스트
     */
    List<BookmarkDTO> getBookmarkDateList(RedisDTO redisDTO, List<String> BookMarkSeqList) throws JsonProcessingException;

    /**
     * 북마크 추가하기
     * @param pDTO 북마크 정보
     */
    void insertBookmark(BookmarkDTO pDTO) throws Exception;

    /**
     * 북마크 해제하기
     * @param pDTO 북마크 정보
     */
    void removeBookmark(BookmarkDTO pDTO) throws Exception;

    /**
     * 북마크가 존재하는지 조회
     * @param pDTO 북마크 정보
     */
    BookmarkDTO getBookmarkExists(BookmarkDTO pDTO) throws Exception;

    /**
     * 북마크 개수 카운트
     * @param userId 카운트할 대상 아이디
     */
    Long bookmarkCount(String userId) throws Exception;


}
