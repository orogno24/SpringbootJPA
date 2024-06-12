package kopo.poly.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.dto.NoticeDTO;
import kopo.poly.dto.NoticeImageDTO;
import kopo.poly.repository.NoticeImageRepository;
import kopo.poly.repository.NoticeRepository;
import kopo.poly.repository.entity.ImagePK;
import kopo.poly.repository.entity.NoticeEntity;
import kopo.poly.repository.entity.NoticeImageEntity;
import kopo.poly.service.INoticeService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class NoticeService implements INoticeService {

    // RequiredArgsConstructor 어노테이션으로 생성자를 자동 생성함
    // noticeRepository 변수에 이미 메모리에 올라간 NoticeRepository 객체를 넣어줌
    // 예전에는 autowired 어노테이션를 통해 설정했었지만, 이젠 생성자를 통해 객체 주입함
    private final NoticeRepository noticeRepository;
    private final NoticeImageRepository noticeImageRepository;

    /**
     * 공지사항 상세 정보가져오기
     *
     * @param pDTO 공지사항 상세 가져오기 위한 정보
     * @param type 조회수 증가여부(true : 증가, false : 증가안함
     */
    @Transactional
    @Override
    public NoticeDTO getNoticeInfo(NoticeDTO pDTO, boolean type) {

        log.info(this.getClass().getName() + ".getNoticeInfo Start!");

        if (type) {
            // 조회수 증가하기
            int res = noticeRepository.updateReadCnt(pDTO.noticeSeq());

            // 조회수 증가 성공여부 체크
            log.info("res : " + res);
        }

        // 공지사항 상세내역 가져오기
        NoticeEntity rEntity = noticeRepository.findByNoticeSeq(pDTO.noticeSeq());

        // 엔티티의 값들을 DTO에 맞게 넣어주기
        NoticeDTO rDTO = new ObjectMapper().convertValue(rEntity, NoticeDTO.class);

        log.info(this.getClass().getName() + ".getNoticeInfo End!");

        return rDTO;
    }

    /**
     * 해당 공지사항 수정하기
     *
     * @param pDTO 공지사항 수정하기 위한 정보
     */
    @Override
    @Transactional
    public void updateNoticeInfo(NoticeDTO pDTO) {

        log.info(this.getClass().getName() + ".updateNoticeInfo Start!");

        Long noticeSeq = pDTO.noticeSeq();

        String title = CmmUtil.nvl(pDTO.title());
        String noticeYn = "N";
        String contents = CmmUtil.nvl(pDTO.contents());
        String userId = CmmUtil.nvl(pDTO.userId());

        log.info("noticeSeq : " + noticeSeq);
        log.info("title : " + title);
        log.info("noticeYn : " + noticeYn);
        log.info("contents : " + contents);
        log.info("userId : " + userId);

        // 현재 공지사항 조회수 가져오기
        NoticeEntity rEntity = noticeRepository.findByNoticeSeq(noticeSeq);

        // 수정할 값들을 빌더를 통해 엔티티에 저장하기
        NoticeEntity pEntity = NoticeEntity.builder()
                .noticeSeq(noticeSeq).title(title).noticeYn(noticeYn).contents(contents).userId(userId)
                .readCnt(rEntity.getReadCnt())
                .regId(rEntity.getUserId()).regDt(rEntity.getRegDt())
                .chgId(userId).chgDt(DateUtil.getDateTime("yyyy-MM-dd hh:mm:ss"))
                .build();

        // 데이터 수정하기 (DB 반영)
        noticeRepository.save(pEntity);

        log.info(this.getClass().getName() + ".updateNoticeInfo End!");

    }

    /**
     * 해당 공지사항 삭제하기
     *
     * @param pDTO 공지사항 삭제하기 위한 정보
     */
    @Override
    public void deleteNoticeInfo(NoticeDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".deleteNoticeInfo Start!");

        Long noticeSeq = pDTO.noticeSeq();

        log.info("noticeSeq : " + noticeSeq);

        // 데이터 수정하기
        noticeRepository.deleteById(noticeSeq);

        log.info(this.getClass().getName() + ".deleteNoticeInfo End!");
    }

    /**
     * 해당 공지사항 저장하기
     *
     * @param pDTO 공지사항 저장하기 위한 정보
     */
    @Override
    @Transactional
    public Long insertNoticeInfo(NoticeDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".InsertNoticeInfo Start!");

        String title = CmmUtil.nvl(pDTO.title());
        String noticeYn = "N";
        String contents = CmmUtil.nvl(pDTO.contents());
        String userId = CmmUtil.nvl(pDTO.userId());

        log.info("title : " + title);
        log.info("noticeYn : " + noticeYn);
        log.info("contents : " + contents);
        log.info("userId : " + userId);

        // 공지사항 저장을 위해서는 PK 값은 빌더에 추가하지 않는다.
        // JPA에 자동 증가 설정을 해놨음
        NoticeEntity pEntity = NoticeEntity.builder()
                .title(title).noticeYn(noticeYn).contents(contents).userId(userId).readCnt(0L)
                .regId(userId).regDt(DateUtil.getDateTime("yyyy-MM-dd hh:mm:ss"))
                .chgId(userId).chgDt(DateUtil.getDateTime("yyyy-MM-dd hh:mm:ss"))
                .build();

        // 공지사항 저장하기
        noticeRepository.save(pEntity);

        log.info(this.getClass().getName() + ".InsertNoticeInfo End!");

        return pEntity.getNoticeSeq();
    }

    /**
     * 이미지 리스트 저장
     *
     * @param noticeSeq 공지사항 번호
     * @param imageDTOs 이미지 리스트
     */
    @Override
    @Transactional
    public void updateNoticeImages(Long noticeSeq, List<NoticeImageDTO> imageDTOs) throws Exception {

        log.info(this.getClass().getName() + ".updateNoticeImages Start!");

        // 기존 이미지 목록을 가져옵니다.
        List<NoticeImageEntity> existingImages = noticeImageRepository.findByNoticeSeq(noticeSeq);

        // 데이터베이스에 이미 있는 이미지들의 처리
        Set<Long> existingImageIds = existingImages.stream()
                .map(NoticeImageEntity::getImageSeq)
                .collect(Collectors.toSet());

        Long nextImageSeq = noticeImageRepository.getNextImageSeq(noticeSeq);

        // 새 이미지 추가 또는 기존 이미지 업데이트
        AtomicLong imageSeqCounter = new AtomicLong(nextImageSeq);
        List<NoticeImageEntity> imagesToSave = imageDTOs.stream()
                .map(dto -> {

                    Long newImageSeq = imageSeqCounter.getAndIncrement(); // nextImageSeq부터 시작해서 1씩 증가함
                    log.info("newImageSeq : " + newImageSeq);

                    // 기존 이미지 엔티티 업데이트 또는 새 엔티티 생성
                    NoticeImageEntity imageEntity = existingImages.stream()
                            .filter(img -> img.getImageSeq() != null && img.getImageSeq().equals(dto.imageSeq()))
                            .findFirst()
                            .orElseGet(() -> NoticeImageEntity.builder()
                                    .imageSeq(newImageSeq)
                                    .noticeSeq(noticeSeq) // 새 이미지의 경우 noticeSeq 설정
                                    .build());

                    // 빌더를 통해 imagePath 설정
                    return NoticeImageEntity.builder()
                            .imageSeq(imageEntity.getImageSeq()) // 기존 ID 유지, null이면 자동 생성
                            .noticeSeq(noticeSeq)
                            .imagePath(dto.imagePath())
                            .build();
                })
                .collect(Collectors.toList());

        noticeImageRepository.saveAll(imagesToSave);

        existingImages.stream()
                .filter(image -> !existingImageIds.contains(image.getImageSeq()))
                .forEach(noticeImageRepository::delete);

        log.info(this.getClass().getName() + ".updateNoticeImages End!");

    }

    /**
     * 해당 게시글의 이미지 리스트 가져오기
     */
    @Override
    @Transactional
    public List<NoticeImageDTO> getImageList(NoticeDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".updateNoticeImages Start!");

        Long noticeSeq = pDTO.noticeSeq();
        log.info("noticeSeq : " + noticeSeq);

        List<NoticeImageEntity> rList = noticeImageRepository.findByNoticeSeq(noticeSeq);

        List<NoticeImageDTO> nList = new ObjectMapper().convertValue(rList,
                new TypeReference<List<NoticeImageDTO>>() {
                });

        log.info(this.getClass().getName() + ".updateNoticeImages End!");

        return nList;
    }

    /**
     * 게시글 이미지 삭제
     */
    @Override
    @Transactional
    public void deleteImageById(NoticeImageDTO pDTO) throws Exception {

        ImagePK imagePK = ImagePK.builder().imageSeq(pDTO.imageSeq()).noticeSeq(pDTO.noticeSeq()).build();

        NoticeImageEntity imageEntity = noticeImageRepository.findById(imagePK)
                .orElseThrow(() -> new Exception("Image not found with ID: " + imagePK));

        // 데이터베이스에서 이미지 레코드 삭제
        noticeImageRepository.delete(imageEntity);
    }

    /**
     * 게시글 이미지 S3에서 삭제하기 위해 이미지 url를 추출
     */
    @Override
    public String getImagePath(NoticeImageDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + "getImagePath Start!");

        ImagePK imagePK = ImagePK.builder().noticeSeq(pDTO.noticeSeq()).imageSeq(pDTO.imageSeq()).build();

        Optional<NoticeImageEntity> rEntity = noticeImageRepository.findById(imagePK);

        String imagePath = rEntity.get().getImagePath();

        log.info("imagePath : " + imagePath);

        log.info(this.getClass().getName() + "getImagePath End!");

        return imagePath;
    }

    /**
     * 게시글을 삭제하면 이미지들을 S3에서 삭제하기 위해 이미지 리스트 추출
     */
    @Override
    public List<NoticeImageDTO> getImagePathList(Long noticeSeq) throws Exception {

        log.info(this.getClass().getName() + "getImagePathList Start!");

        List<NoticeImageEntity> rList = noticeImageRepository.findByNoticeSeq(noticeSeq);

        List<NoticeImageDTO> nList = new ObjectMapper().convertValue(rList,
                new TypeReference<List<NoticeImageDTO>>() {
                });

        log.info(this.getClass().getName() + "getImagePathList End!");

        return nList;
    }

    /**
     * 특정 userId 기준 게시글 개수 조회
     */
    @Override
    public long countByUserId(String userId) throws Exception {
        return noticeRepository.countByUserId(userId);
    }
}
