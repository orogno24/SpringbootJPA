package kopo.poly.persistance.mongodb;

import kopo.poly.dto.CultureDTO;

import java.util.List;

public interface ICultureMapper {

    /**
     * 좌표 기준 문화시설 리스트 조회
     *
     * @param colNm 조회할 컬렉션 이름
     * @param pDTO 검색할 조건 목록
     * @return 문화시설 리스트
     */
    List<CultureDTO> getCultureListNearby(String colNm, CultureDTO pDTO) throws Exception;

    /**
     * 문화시설 상세정보 조회
     *
     * @param colNm 조회할 컬렉션 이름
     * @return 문화시설 리스트
     */
    CultureDTO getCultureInfo(String colNm, String nSeq) throws Exception;

}
