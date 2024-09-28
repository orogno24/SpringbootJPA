package kopo.poly.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kopo.poly.dto.BookmarkDTO;
import kopo.poly.dto.NetworkDTO;
import kopo.poly.dto.RedisDTO;
import kopo.poly.dto.ScheduleDTO;

import java.util.List;

public interface INetworkService {

    /**
     * 모임 정보 DB에 추가하기
     *
     * @param pDTO 모임 저장하기 위한 정보
     */
    Long insertNetWorkInfo(NetworkDTO pDTO) throws Exception;

    /**
     * 최대 인원 조회하기
     *
     * @param eventSeq 해당하는 일정 번호
     */
    NetworkDTO countParticipants(String eventSeq);

    /**
     * 일정의 현재 인원 변경
     */
    void countChange(Boolean add, String eventSeq) throws Exception;

    /**
     * NativeQuery를 사용하여 특정 기준 네트워크 리스트 가져오기
     */
    List<NetworkDTO> getNetworkList(String type) throws Exception;

    /**
     * 네트워크 리스트 전체 가져오기
     */
    List<NetworkDTO> getAllNetworkList() throws Exception;

    /**
     * NativeQuery를 사용하여 네크워크 상세정보 가져오기
     */
    NetworkDTO getNetworkInfo(String networkSeq) throws Exception;

    /**
     * 해당 일정 삭제하기
     */
    void networkDelete(NetworkDTO pDTO) throws Exception;

    /**
     * 일정 북마크 추가하기
     * @param pDTO 북마크 정보
     */
    void insertBookmark(ScheduleDTO pDTO) throws Exception;

    /**
     * 일정 북마크 해제하기
     * @param pDTO 북마크 정보
     */
    void removeBookmark(ScheduleDTO pDTO) throws Exception;

    /**
     * 일정 북마크가 존재하는지 조회
     * @param pDTO 북마크 정보
     */
    ScheduleDTO getBookmarkExists(ScheduleDTO pDTO) throws Exception;

    /**
     * 북마크한 유저들 리스트 조회
     * @param networkSeq 일정 seq
     */
    List<String> getBookmarkUsers(String userId, String networkSeq) throws Exception;

    /**
     * 북마크에서 Seq만 추출 후 List에 담기
     * @param pDTO 유저아이디
     * @return Seq 리스트
     */
    List<ScheduleDTO> getScheduleSeq(ScheduleDTO pDTO) throws Exception;

    /**
     * Seq List를 기준으로 전체 데이터 필터링
     */
    List<NetworkDTO> getNetworkDateList(List<NetworkDTO> networkList, List<Long> BookMarkSeqList) throws JsonProcessingException;

    /**
     * 북마크 개수 카운트
     * @param userId 카운트할 대상 아이디
     */
    Long scheduleCount(String userId) throws Exception;

}
