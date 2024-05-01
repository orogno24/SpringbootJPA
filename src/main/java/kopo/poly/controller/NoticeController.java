package kopo.poly.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.CommentDTO;
import kopo.poly.dto.MsgDTO;
import kopo.poly.dto.NoticeDTO;
import kopo.poly.dto.NoticeImageDTO;
import kopo.poly.service.INoticeService;
import kopo.poly.service.IUserInfoService;
import kopo.poly.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


/*
 * Controller 선언해야만 Spring 프레임워크에서 Controller인지 인식 가능
 * 자바 서블릿 역할 수행
 *
 * slf4j는 스프링 프레임워크에서 로그 처리하는 인터페이스 기술이며,
 * 로그처리 기술인 log4j와 logback과 인터페이스 역할 수행함
 * 스프링 프레임워크는 기본으로 logback을 채택해서 로그 처리함
 * */
@Slf4j
@RequestMapping(value = "/notice")
@RequiredArgsConstructor
@Controller
public class NoticeController {

    // @RequiredArgsConstructor 를 통해 메모리에 올라간 서비스 객체를 Controller에서 사용할 수 있게 주입함
    private final INoticeService noticeService;

    private final AmazonS3 s3Client;

    private final String bucketName;

    @Autowired
    public NoticeController(AmazonS3 s3Client, String bucketName, INoticeService noticeService) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.noticeService = noticeService;
    }

    /**
     * 게시판 리스트 보여주기
     * <p>
     * GetMapping(value = "notice/noticeList") =>  GET방식을 통해 접속되는 URL이 notice/noticeList 경우 아래 함수를 실행함
     */
    @GetMapping(value = "noticeList")
    public String noticeList(HttpSession session, ModelMap model)
            throws Exception {

        // 로그 찍기(추후 찍은 로그를 통해 이 함수에 접근했는지 파악하기 용이하다.)
        log.info(this.getClass().getName() + ".noticeList Start!");

        // 로그인된 사용자 아이디는 Session에 저장함
        // 교육용으로 아직 로그인을 구현하지 않았기 때문에 Session에 데이터를 저장하지 않았음
        // 추후 로그인을 구현할 것으로 가정하고, 공지사항 리스트 출력하는 함수에서 로그인 한 것처럼 Session 값을 생성함

        // 공지사항 리스트 조회하기
        // Java 8부터 제공되는 Optional 활용하여 NPE(Null Pointer Exception) 처리
        List<NoticeDTO> rList = Optional.ofNullable(noticeService.getNoticeList())
                .orElseGet(ArrayList::new);

        // 조회된 리스트 결과값 넣어주기
        model.addAttribute("rList", rList);

        // 로그 찍기(추후 찍은 로그를 통해 이 함수 호출이 끝났는지 파악하기 용이하다.)
        log.info(this.getClass().getName() + ".noticeList End!");

        // 함수 처리가 끝나고 보여줄 HTML (Thymeleaf) 파일명
        // templates/notice/noticeList.html
        return "notice/noticeListUsingQueryDSL";

    }

    /**
     * 게시판 작성 페이지 이동
     * <p>
     * 이 함수는 게시판 작성 페이지로 접근하기 위해 만듬
     * <p>
     * GetMapping(value = "notice/noticeReg") =>  GET방식을 통해 접속되는 URL이 notice/noticeReg 경우 아래 함수를 실행함
     */
    @GetMapping(value = "noticeReg")
    public String noticeReg() {

        log.info(this.getClass().getName() + ".noticeReg Start!");

        log.info(this.getClass().getName() + ".noticeReg End!");

        // 함수 처리가 끝나고 보여줄 HTML (Thymeleaf) 파일명
        // templates/notice/noticeReg.html
        return "notice/noticeReg";
    }

    /**
     * 게시판 글 등록
     * <p>
     * 게시글 등록은 Ajax로 호출되기 때문에 결과는 JSON 구조로 전달해야만 함
     * JSON 구조로 결과 메시지를 전송하기 위해 @ResponseBody 어노테이션 추가함
     */
    @PostMapping(value = "noticeInsert")
    public ResponseEntity<?> noticeInsert(HttpServletRequest request, @RequestParam("images") MultipartFile[] images, HttpSession session) {
        log.info(this.getClass().getName() + ".noticeInsert Start!");

        try {
            String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
            String title = CmmUtil.nvl(request.getParameter("title"));
            String contents = CmmUtil.nvl(request.getParameter("contents"));
            List<NoticeImageDTO> imageDTOList = new ArrayList<>();

            // 게시글 DTO 생성
            NoticeDTO pDTO = NoticeDTO.builder()
                    .userId(userId)
                    .title(title)
                    .contents(contents)
                    .images(imageDTOList)
                    .build();

            // 게시글 정보 저장
            Long noticeSeq = noticeService.insertNoticeInfo(pDTO);

            // 이미지 처리 및 데이터베이스 저장
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String extension = FilenameUtils.getExtension(image.getOriginalFilename());
                    String fileName = "notices/" + userId + "_" + noticeSeq + "_" + UUID.randomUUID().toString() + "." + extension;

                    s3Client.putObject(new PutObjectRequest(bucketName, fileName, image.getInputStream(), null)
                            .withCannedAcl(CannedAccessControlList.PublicRead));
                    String imageUrl = s3Client.getUrl(bucketName, fileName).toString();

                    NoticeImageDTO noticeImageDTO = new NoticeImageDTO(null, noticeSeq, imageUrl);
                    imageDTOList.add(noticeImageDTO);
                }
            }

            // 이미지 정보 업데이트 (데이터베이스에 이미지 정보 저장)
            noticeService.updateNoticeImages(noticeSeq, imageDTOList);

            return ResponseEntity.ok("게시글 등록에 성공했습니다.");
        } catch (Exception e) {
            log.error("게시글 등록 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 등록에 실패했습니다. : " + e.getMessage());
        } finally {
            log.info(this.getClass().getName() + ".noticeInsert End!");
        }
    }


    /**
     * 게시판 상세보기
     */
    @GetMapping(value = "noticeInfo")
    public String noticeInfo(HttpServletRequest request, ModelMap model) throws Exception {

        log.info(this.getClass().getName() + ".noticeInfo Start!");

        String nSeq = CmmUtil.nvl(request.getParameter("nSeq"), "0"); // 공지글번호(PK)

        log.info("nSeq : " + nSeq);

        NoticeDTO pDTO = NoticeDTO.builder().noticeSeq(Long.parseLong(nSeq)).build();

        // 공지사항 상세정보 가져오기
        // Java 8부터 제공되는 Optional 활용하여 NPE(Null Pointer Exception) 처리
        NoticeDTO rDTO = Optional.ofNullable(noticeService.getNoticeInfo(pDTO, true))
                .orElseGet(() -> NoticeDTO.builder().build());

        // 조회된 리스트 결과값 넣어주기
        model.addAttribute("rDTO", rDTO);

        log.info(this.getClass().getName() + ".noticeInfo End!");

        return "notice/noticeInfo";
    }

    /**
     * 게시판 수정 보기
     */
    @GetMapping(value = "noticeEditInfo")
    public String noticeEditInfo(HttpServletRequest request, ModelMap model) throws Exception {

        log.info(this.getClass().getName() + ".noticeEditInfo Start!");

        String nSeq = CmmUtil.nvl(request.getParameter("nSeq")); // 공지글번호(PK)

        log.info("nSeq : " + nSeq);

        NoticeDTO pDTO = NoticeDTO.builder().noticeSeq(Long.parseLong(nSeq)).build();
        NoticeDTO rDTO = Optional.ofNullable(noticeService.getNoticeInfo(pDTO, false))
                .orElseGet(() -> NoticeDTO.builder().build());

        List<NoticeImageDTO> iList = Optional.ofNullable(noticeService.getImageList(pDTO))
                .orElseGet(ArrayList::new);

        model.addAttribute("rDTO", rDTO);
        model.addAttribute("iList", iList);

        log.info(this.getClass().getName() + ".noticeEditInfo End!");

        return "notice/noticeEditInfo";
    }

    /**
     * 게시판 글 수정
     */
    @PostMapping(value = "noticeUpdate")
    public ResponseEntity<?> noticeUpdate(HttpServletRequest request, @RequestParam("images") MultipartFile[] images, HttpSession session) {

        log.info(this.getClass().getName() + ".noticeUpdate Start!");

        try {
            String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID")); // 아이디
            Long noticeSeq = Long.parseLong(CmmUtil.nvl(request.getParameter("nSeq"))); // 글번호(PK)
            String title = CmmUtil.nvl(request.getParameter("title")); // 제목
            String contents = CmmUtil.nvl(request.getParameter("contents")); // 내용
            List<NoticeImageDTO> imageDTOList = new ArrayList<>();

            log.info("userId : " + userId);
            log.info("noticeSeq : " + noticeSeq);
            log.info("title : " + title);
            log.info("contents : " + contents);

            NoticeDTO pDTO = NoticeDTO.builder()
                    .userId(userId)
                    .noticeSeq(noticeSeq)
                    .title(title)
                    .contents(contents)
                    .images(imageDTOList)
                    .build();

            // 게시글 수정하기 DB
            noticeService.updateNoticeInfo(pDTO);

            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String extension = FilenameUtils.getExtension(image.getOriginalFilename());
                    String fileName = "notices/" + userId + "_" + noticeSeq + "_" + UUID.randomUUID().toString() + "." + extension;

                    s3Client.putObject(new PutObjectRequest(bucketName, fileName, image.getInputStream(), null)
                            .withCannedAcl(CannedAccessControlList.PublicRead));
                    String imageUrl = s3Client.getUrl(bucketName, fileName).toString();

                    NoticeImageDTO noticeImageDTO = new NoticeImageDTO(null, noticeSeq, imageUrl);
                    imageDTOList.add(noticeImageDTO);
                }
            }

            noticeService.updateNoticeImages(noticeSeq, imageDTOList);

            return ResponseEntity.ok("게시글 수정에 성공했습니다.");
        } catch (Exception e) {
            log.error("게시글 수정 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 수정에 실패했습니다. : " + e.getMessage());
        } finally {
            log.info(this.getClass().getName() + ".noticeUpdate End!");
        }
    }

    /**
     * 게시판 글 삭제
     */
    @ResponseBody
    @PostMapping(value = "noticeDelete")
    public MsgDTO noticeDelete(HttpServletRequest request) {

        log.info(this.getClass().getName() + ".noticeDelete Start!");

        String msg = ""; // 메시지 내용
        MsgDTO dto = null; // 결과 메시지 구조

        try {
            String nSeq = CmmUtil.nvl(request.getParameter("nSeq")); // 글번호(PK)

            /*
             * ####################################################################################
             * 반드시, 값을 받았으면, 꼭 로그를 찍어서 값이 제대로 들어오는지 파악해야함 반드시 작성할 것
             * ####################################################################################
             */
            log.info("nSeq : " + nSeq);

            /*
             * 값 전달은 반드시 DTO 객체를 이용해서 처리함 전달 받은 값을 DTO 객체에 넣는다.
             */
            NoticeDTO pDTO = NoticeDTO.builder().noticeSeq(Long.parseLong(nSeq)).build();

            // 게시글 삭제하기 DB
            noticeService.deleteNoticeInfo(pDTO);

            msg = "삭제되었습니다.";

        } catch (Exception e) {
            msg = "실패하였습니다. : " + e.getMessage();
            log.info(e.toString());
            e.printStackTrace();

        } finally {

            // 결과 메시지 전달하기
            dto = MsgDTO.builder().msg(msg).build();

            log.info(this.getClass().getName() + ".noticeDelete End!");

        }

        return dto;
    }

    @DeleteMapping("deleteImage/{imageSeq}")
    public ResponseEntity<?> deleteImage(@PathVariable("imageSeq") Long imageSeq) {
        try {
            log.info("imageSeq : " + imageSeq);
            noticeService.deleteImageById(imageSeq);
            return ResponseEntity.ok().body("이미지 삭제 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("이미지 삭제 실패: " + e.getMessage());
        }
    }

}
