package kopo.poly.service;

import kopo.poly.dto.CultureDTO;

import java.util.List;

public interface ICultureService {

    /**
     * 문화시설 리스트 가져오기
     */
    List<CultureDTO> getCultureList() throws Exception;
}
