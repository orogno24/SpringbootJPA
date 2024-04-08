package kopo.poly.service;

import kopo.poly.dto.EventDTO;
import kopo.poly.dto.NoticeDTO;

import java.util.List;

public interface IEventService {

    /**
     * 문화행사 전체 가져오기
     */
    List<EventDTO> getEventList();

    /**
     * 문화행사 상세 정보가져오기
     *
     * @param pDTO 문화행사 상세 가져오기 위한 정보
     * @param type 조회수 증가여부(true : 증가, false : 증가안함
     */
    EventDTO getEventInfo(EventDTO pDTO, boolean type) throws Exception;

}
