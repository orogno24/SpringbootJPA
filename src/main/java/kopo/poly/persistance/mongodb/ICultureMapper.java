package kopo.poly.persistance.mongodb;

import kopo.poly.dto.CultureDTO;

import java.util.List;

public interface ICultureMapper {

    /**
     * 문화시설 리스트 가져오기
     *
     * @param colNm 조회할 컬렉션 이름
     * @return 문화시설 리스트
     */
    List<CultureDTO> getCultureList(String colNm) throws Exception;

    /**
     * 좌표 기준 반경 3km 문화시설 조회
     *
     * @param colNm 조회할 컬렉션 이름
     * @param pDTO 검색할 조건 목록
     * @return 문화시설 리스트
     */
    List<CultureDTO> getCultureListNearby(String colNm, CultureDTO pDTO) throws Exception;

}
