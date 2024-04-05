package kopo.poly.service;

import kopo.poly.dto.CommentDTO;
import kopo.poly.dto.NoticeDTO;

import java.util.List;

public interface INoticeJoinService {

    /**
     * QueryDSL 활용한 Fetch Join 적용된 공지사항 전체 가져오기
     */
    List<NoticeDTO> getNoticeListForQueryDSL();

    /**
     * QueryDSL 활용한 공지사항 싱세 정보가져오기
     *
     * @param pDTO 공지사항 상세 가져오기 위한 정보
     * @param type 조회수 증가여부(true : 증가, false : 증가안함
     */
    NoticeDTO getNoticeInfoForQueryDSL(NoticeDTO pDTO, boolean type) throws Exception;

    List<CommentDTO> getCommentForQueryDSL(CommentDTO cDTO);

    /**
     * 해당 댓글 삭제하기
     *
     * @param pDTO 댓글 삭제하기 위한 정보
     */
    void deleteComment(CommentDTO pDTO) throws Exception;

    /**
     * 해당 댓글 저장하기
     *
     * @param pDTO 댓글 저장하기 위한 정보
     */
    void insertComment(CommentDTO pDTO) throws Exception;

}
