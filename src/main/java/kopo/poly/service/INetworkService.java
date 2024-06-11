package kopo.poly.service;

import kopo.poly.dto.NetworkDTO;

import java.util.List;

public interface INetworkService {

    /**
     * 모임 정보 DB에 추가하기
     *
     * @param pDTO 모임 저장하기 위한 정보
     */
    void insertNetWorkInfo(NetworkDTO pDTO) throws Exception;

    /**
     * NativeQuery를 사용하여 네트워크 리스트 전체 가져오기
     */
    List<NetworkDTO> getNetworkList(String type) throws Exception;

    /**
     * NativeQuery를 사용하여 네크워크 상세정보 가져오기
     */
    NetworkDTO getNetworkInfo(String networkSeq) throws Exception;

}
