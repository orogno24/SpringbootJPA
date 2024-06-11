package kopo.poly.service;

import kopo.poly.dto.NetworkDTO;

public interface INetworkService {

    /**
     * 모임 정보 DB에 추가하기
     *
     * @param pDTO 모임 저장하기 위한 정보
     */
    void insertNetWorkInfo(NetworkDTO pDTO) throws Exception;

}
