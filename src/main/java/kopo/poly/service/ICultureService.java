package kopo.poly.service;

import kopo.poly.dto.CultureDTO;

import java.util.List;

public interface ICultureService {

    /**
     * 좌표 기준 문화시설 리스트 가져오기
     */
    List<CultureDTO> getCultureListNearby(CultureDTO pDTO) throws Exception;

    /**
     * 문화시설 상세정보 조회
     */
    CultureDTO getCultureInfo(String nSeq) throws Exception;

}
