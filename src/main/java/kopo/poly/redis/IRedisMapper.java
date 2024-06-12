package kopo.poly.redis;

import kopo.poly.dto.RedisDTO;

import java.util.List;
import java.util.Map;

public interface IRedisMapper {

    /**
     * 문화행사 리스트 저장하기
     *
     * @param rContent 저장할 데이터
     * @param colNm    저장할 키
     */
    void insertEventList(List<Map<String, Object>> rContent, String colNm) throws Exception;

    /**
     * 키 정보 존재여부 체크하기
     *
     * @param colNm 저장된 키 이름
     * @return key 존재여부
     */
    boolean getExistKey(String colNm) throws Exception;

    /**
     * 문화행사 리스트 가져오기
     *
     * @param colNm 저장된 키 이름
     * @return 문화행사 리스트
     */
    RedisDTO getEventList(String colNm) throws Exception;

}
