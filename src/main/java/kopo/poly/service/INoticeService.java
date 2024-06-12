package kopo.poly.service;

import kopo.poly.dto.NoticeDTO;
import kopo.poly.dto.NoticeImageDTO;

import java.util.List;

public interface INoticeService {

    /**
     * 공지사항 상세 정보가져오기
     *
     * @param pDTO 공지사항 상세 가져오기 위한 정보
     * @param type 조회수 증가여부(true : 증가, false : 증가안함
     */
    NoticeDTO getNoticeInfo(NoticeDTO pDTO, boolean type) throws Exception;

    /**
     * 해당 공지사항 수정하기
     *
     * @param pDTO 공지사항 수정하기 위한 정보
     */
    void updateNoticeInfo(NoticeDTO pDTO) throws Exception;

    /**
     * 해당 공지사항 삭제하기
     *
     * @param pDTO 공지사항 삭제하기 위한 정보
     */
    void deleteNoticeInfo(NoticeDTO pDTO) throws Exception;

    /**
     * 해당 공지사항 저장하기
     *
     * @param pDTO 공지사항 저장하기 위한 정보
     */
    Long insertNoticeInfo(NoticeDTO pDTO) throws Exception;

    /**
     * 이미지 리스트 저장
     *
     * @param noticeSeq 공지사항 번호
     * @param imageDTOs 이미지 리스트
     */
    void updateNoticeImages(Long noticeSeq, List<NoticeImageDTO> imageDTOs) throws Exception;

    /**
     * 해당 게시글의 이미지 리스트 가져오기
     */
    List<NoticeImageDTO> getImageList(NoticeDTO pDTO) throws Exception;

    /**
     * 게시글 이미지 삭제
     */
    void deleteImageById(NoticeImageDTO pDTO) throws Exception;

    /**
     * 게시글 이미지 S3에서 삭제하기 위해 이미지 url를 추출
     */
    String getImagePath(NoticeImageDTO pDTO) throws Exception;

    /**
     * 게시글을 삭제하면 이미지들을 S3에서 삭제하기 위해 이미지 리스트 추출
     */
    List<NoticeImageDTO> getImagePathList(Long noticeSeq) throws Exception;

    /**
     * 특정 userId 기준 게시글 개수 조회
     */
    long countByUserId(String userId) throws Exception;

}
