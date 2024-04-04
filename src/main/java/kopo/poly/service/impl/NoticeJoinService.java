package kopo.poly.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import kopo.poly.dto.NoticeDTO;
import kopo.poly.repository.NoticeRepository;
import kopo.poly.repository.NoticeSQLRepository;
import kopo.poly.repository.entity.*;
import kopo.poly.service.INoticeJoinService;
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

    private final NoticeSQLRepository noticeSQLRepository;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<NoticeDTO> getNoticeListUsingNativeQuery() {
        log.info(this.getClass().getName() + ".getNoticeListUsingNativeQuery Start!");

        List<NoticeSQLEntity> rList = noticeSQLRepository.getNoticeListUsingSQL();

        List<NoticeDTO> nList = new ObjectMapper().convertValue(rList,
                new TypeReference<List<NoticeDTO>>() {
                });

        log.info(this.getClass().getName() + ".getNoticeListUsingNativeQuery End!");

        return nList;
    }

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
                    .userName(e.getUserInfo().getUserName()).build();
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

        QNoticeEntity ne = QNoticeEntity.noticeEntity;

        NoticeEntity rEntity = queryFactory
                .selectFrom(ne)
                .where(ne.noticeSeq.eq(pDTO.noticeSeq()))
                .fetchOne();

        NoticeDTO rDTO = NoticeDTO.builder().noticeSeq(rEntity.getNoticeSeq())
                .title(rEntity.getTitle())
                .noticeYn(rEntity.getNoticeYn())
                .regDt(rEntity.getRegDt())
                .userId(rEntity.getUserId())
                .readCnt(rEntity.getReadCnt())
                .contents(rEntity.getContents()).build();

        log.info(this.getClass().getName() + ".getNoticeInfoForQueryDSL End!");

        return rDTO;
    }
}
