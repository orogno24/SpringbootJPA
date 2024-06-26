package kopo.poly.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kopo.poly.dto.BookmarkDTO;
import kopo.poly.dto.CommentDTO;
import kopo.poly.dto.NoticeDTO;

import java.util.List;

public interface INoticeJoinService {

    /**
     * QueryDSL 활용한 Fetch Join 적용된 공지사항 전체 가져오기
     */
    List<NoticeDTO> getNoticeListForQueryDSL();

    /**
     * NativeQuery 사용하여 전체 게시글 가져오기
     */
    List<NoticeDTO> getNoticeListUsingNativeQuery();

    /**
     * 팔로우한 사용자의 게시글만 가져오기
     */
    List<NoticeDTO> getFollowNoticeList(List<String> followUserIdList);

    /**
     * NativeQuery 사용하여 내 게시글만 가져오기
     */
    List<NoticeDTO> getUserNoticeListUsingNativeQuery(String userId);

    /**
     * QueryDSL 활용한 공지사항 상세 정보가져오기
     *
     * @param pDTO 공지사항 상세 가져오기 위한 정보
     * @param type 조회수 증가여부(true : 증가, false : 증가안함
     */
    NoticeDTO getNoticeInfoForQueryDSL(NoticeDTO pDTO, boolean type) throws Exception;

    List<CommentDTO> getCommentForQueryDSL(CommentDTO cDTO);

    /**
     * 해당 댓글 수정
     *
     * @param pDTO 댓글 수정하기 위한 정보
     */
    void updateComment(CommentDTO pDTO) throws Exception;

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
