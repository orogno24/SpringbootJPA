package kopo.poly.redis;

import kopo.poly.dto.EventDTO;
import kopo.poly.dto.RedisDTO;

import java.util.List;
import java.util.Map;

public interface IRedisMapper {

    /**
     * 멜론 노래 리스트 저장하기
     *
     * @param rContent 저장할 데이터
     * @param colNm 저장할 키
     * @return 저장 결과
     */
    int insertEventList(List<Map<String, Object>> rContent, String colNm) throws Exception;

    /**
     * 멜론 노래 키 정보 존재여부 체크하기
     *
     * @param colNm 저장된 키 이름
     * @return key존재여부
     */
    boolean getExistKey(String colNm) throws Exception;

    /**
     * 오늘 수집된 멜론 노래리스트 가져오기
     *
     * @param colNm 저장된 키 이름
     * @return 노래 리스트
     */
    RedisDTO getEventList(String colNm) throws Exception;

}
