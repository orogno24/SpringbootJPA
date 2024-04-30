package kopo.poly.service.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import kopo.poly.dto.CommentDTO;
import kopo.poly.dto.NoticeDTO;
import kopo.poly.repository.CommentRepository;
import kopo.poly.repository.NoticeRepository;
import kopo.poly.repository.entity.*;
import kopo.poly.service.INoticeJoinService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class NoticeJoinService implements INoticeJoinService {

    private final NoticeRepository noticeRepository;
    private final CommentRepository commentRepository;

    private final JPAQueryFactory queryFactory;

    @Transactional
    @Override
    public List<NoticeDTO> getNoticeListForQueryDSL() {
        log.info(this.getClass().getName() + ".getNoticeListForQueryDSL Start!");

        // QueryDSL 라이브러리를 추가하면, JPA 엔티티들은 Q붙여서 QueryDSL에서 처리가능한 객체를 생성함
        // 예 : NoticeEntity -> QNoticeEntity 객체 생성
        QNoticeFetchEntity ne = QNoticeFetchEntity.noticeFetchEntity;
        QUserInfoEntity ue = QUserInfoEntity.userInfoEntity;

        // 공지사항 전체 리스트 조회하기
        List<NoticeFetchEntity> rList = queryFactory
                .selectFrom(ne) // 조회할 Entity 및 항목 정의
                .join(ne.userInfo, ue) // Inner Join 적용
                // 공지사항은 위로, 공지사항이 아닌 글들은 아래로 정렬한 뒤, 글 순번이 큰 순서대로 정렬
                .orderBy(ne.noticeYn.desc(), ne.noticeSeq.desc())
                .fetch(); // 결과를 리스트 구조로 반환하기

        List<NoticeDTO> nList = new ArrayList<>();

        rList.forEach(e -> {
            NoticeDTO rDTO = NoticeDTO.builder().
                    noticeSeq(e.getNoticeSeq()).title(e.getTitle()).noticeYn(e.getNoticeYn())
                    .readCnt(e.getReadCnt()).userId(e.getUserId())
                    .regDt(e.getRegDt())
                    .chgDt(e.getChgDt())
                    .userName(e.getUserInfo().getUserName())
                    .profilePath(e.getUserInfo().getProfilePath())
                    .build();
            nList.add(rDTO);
        });

        log.info(this.getClass().getName() + ".getNoticeListForQueryDSL End!");

        return nList;
    }

    @Transactional
    @Override
    public NoticeDTO getNoticeInfoForQueryDSL(NoticeDTO pDTO, boolean type) throws Exception {
        log.info(this.getClass().getName() + ".getNoticeInfoForQueryDSL Start!");

        if (type) {
            int res = noticeRepository.updateReadCnt(pDTO.noticeSeq());

            log.info("res : " + res);
        }

        QNoticeFetchEntity ne = QNoticeFetchEntity.noticeFetchEntity;
        QUserInfoEntity ue = QUserInfoEntity.userInfoEntity;

        NoticeFetchEntity rEntity = queryFactory
                .selectFrom(ne)
                .join(ne.userInfo, ue)
                .where(ne.noticeSeq.eq(pDTO.noticeSeq()))
                .fetchOne();

//        SELECT N.NOTICE_SEQ, N.TITLE, N.NOTICE_YN, N.READ_CNT, U.USER_NAME, U.EXP, N.USER_ID,
//                CONTENTS, DATE_FORMAT(N.REG_DT, '%Y-%m-%d') AS REG_DT
//        FROM NOTICE N, USER_INFO U
//        WHERE N.USER_ID = U.USER_ID
//        AND N.NOTICE_SEQ = #{noticeSeq}

        NoticeDTO rDTO = NoticeDTO.builder().noticeSeq(rEntity.getNoticeSeq())
                .title(rEntity.getTitle())
                .noticeYn(rEntity.getNoticeYn())
                .regDt(rEntity.getRegDt())
                .userId(rEntity.getUserId())
                .readCnt(rEntity.getReadCnt())
                .userName(rEntity.getUserInfo().getUserName())
                .contents(rEntity.getContents()).build();

        log.info(this.getClass().getName() + ".getNoticeInfoForQueryDSL End!");

        return rDTO;
    }

    @Transactional
    @Override
    public List<CommentDTO> getCommentForQueryDSL(CommentDTO cDTO) {
        log.info(this.getClass().getName() + ".getCommentForQueryDSL Start!");

        // QueryDSL 라이브러리를 추가하면, JPA 엔티티들은 Q붙여서 QueryDSL에서 처리가능한 객체를 생성함
        // 예 : NoticeEntity -> QNoticeEntity 객체 생성
        QCommentEntity ne = QCommentEntity.commentEntity;
        QUserInfoEntity ue = QUserInfoEntity.userInfoEntity;

        // 공지사항 전체 리스트 조회하기
        List<CommentEntity> rList = queryFactory
                .selectFrom(ne) // 조회할 Entity 및 항목 정의
                .join(ne.userInfo, ue) // Inner Join 적용
                .where(ne.noticeSeq.eq(cDTO.noticeSeq()))
                .fetch(); // 결과를 리스트 구조로 반환하기

        List<CommentDTO> nList = new ArrayList<>();

        rList.forEach(e -> {
            CommentDTO rDTO = CommentDTO.builder().
                    commentSeq(e.getCommentSeq()).noticeSeq(e.getNoticeSeq()).userId(e.getUserId())
                    .contents(e.getContents()).regDt(e.getRegDt())
                    .userName(e.getUserInfo().getUserName()).build();
            nList.add(rDTO);
        });

        log.info(this.getClass().getName() + ".getCommentForQueryDSL End!");

        return nList;
    }

    @Override
    public void deleteComment(CommentDTO pDTO) throws Exception {
        log.info(this.getClass().getName() + ".deleteComment Start!");

        Long commentSeq = pDTO.commentSeq();

        log.info("commentSeq : " + commentSeq);

        // 데이터 수정하기
        commentRepository.deleteById(commentSeq);

        log.info(this.getClass().getName() + ".deleteComment End!");
    }

    @Override
    public void insertComment(CommentDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".insertComment Start!");

        String userId = CmmUtil.nvl(pDTO.userId());
        String contents = CmmUtil.nvl(pDTO.contents());
        String nSeq = CmmUtil.nvl(String.valueOf(pDTO.noticeSeq()));

        log.info("pDTO userId : " + userId);
        log.info("pDTO contents : " + contents);
        log.info("pDTO nSeq : " + nSeq);

        // 공지사항 저장을 위해서는 PK 값은 빌더에 추가하지 않는다.
        // JPA에 자동 증가 설정을 해놨음
        CommentEntity pEntity = CommentEntity.builder()
                .userId(userId)
                .contents(contents)
                .noticeSeq(pDTO.noticeSeq())
                .regDt(DateUtil.getDateTime("yyyy-MM-dd hh:mm:ss"))
                .build();

        // 공지사항 저장하기
        commentRepository.save(pEntity);

        log.info(this.getClass().getName() + ".insertComment End!");

    }
}
