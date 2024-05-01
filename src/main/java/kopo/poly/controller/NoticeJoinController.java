package kopo.poly.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.CommentDTO;
import kopo.poly.dto.MsgDTO;
import kopo.poly.dto.NoticeDTO;
import kopo.poly.dto.NoticeImageDTO;
import kopo.poly.service.INoticeJoinService;
import kopo.poly.service.INoticeService;
import kopo.poly.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
public class NoticeJoinController {

    // @RequiredArgsConstructor 를 통해 메모리에 올라간 서비스 객체를 Controller에서 사용할 수 있게 주입함
    private final INoticeService noticeService;
    private final INoticeJoinService noticeJoinService;

    @GetMapping(value = "noticeListUsingQueryDSL")
    public String noticeListUsingQueryDSL(HttpSession session, ModelMap model)
            throws Exception {

        log.info(this.getClass().getName() + ".noticeListUsingQueryDSL Start!");

        List<NoticeDTO> rList = Optional.ofNullable(noticeJoinService.getNoticeListForQueryDSL())
                .orElseGet(ArrayList::new);

        model.addAttribute("rList", rList);

        log.info(this.getClass().getName() + ".noticeListUsingQueryDSL End!");

        return "notice/noticeListUsingQueryDSL";
    }

    @GetMapping(value = "noticeListUsingNativeQuery")
    public String noticeListUsingNativeQuery(HttpSession session, ModelMap model)
            throws Exception {

        log.info(this.getClass().getName() + ".noticeListUsingNativeQuery Start!");

        List<NoticeDTO> rList = Optional.ofNullable(noticeJoinService.getNoticeListUsingNativeQuery())
                .orElseGet(ArrayList::new);

        model.addAttribute("rList", rList);

        log.info(this.getClass().getName() + ".noticeListUsingNativeQuery End!");

        return "notice/noticeListUsingNativeQuery";
    }

    @GetMapping(value = "noticeInfoUsingQueryDSL")
    public String noticeInfo(HttpServletRequest request, ModelMap model) throws Exception {

        log.info(this.getClass().getName() + ".NoticeInfoUsingQueryDSL Start!");

        String nSeq = CmmUtil.nvl(request.getParameter("nSeq"), "0"); // 공지글번호(PK)

        log.info("nSeq : " + nSeq);

        NoticeDTO pDTO = NoticeDTO.builder().noticeSeq(Long.parseLong(nSeq)).build();
        CommentDTO cDTO = CommentDTO.builder().noticeSeq(Long.parseLong(nSeq)).build();

        // 공지사항 상세정보 가져오기
        // Java 8부터 제공되는 Optional 활용하여 NPE(Null Pointer Exception) 처리
        NoticeDTO rDTO = Optional.ofNullable(noticeJoinService.getNoticeInfoForQueryDSL(pDTO, true))
                .orElseGet(() -> NoticeDTO.builder().build());

        List<NoticeImageDTO> iList = Optional.ofNullable(noticeService.getImageList(pDTO))
                .orElseGet(ArrayList::new);

        List<CommentDTO> cList = Optional.ofNullable(noticeJoinService.getCommentForQueryDSL(cDTO))
                .orElseGet(ArrayList::new);

        // 조회된 리스트 결과값 넣어주기
        model.addAttribute("rDTO", rDTO);
        model.addAttribute("iList", iList);
        model.addAttribute("cList", cList);

        log.info(this.getClass().getName() + ".NoticeInfoUsingQueryDSL End!");

        return "notice/noticeInfoUsingQueryDSL";
    }

    @ResponseBody
    @PostMapping(value = "deleteComment")
    public MsgDTO deleteComment(HttpServletRequest request) {

        log.info(this.getClass().getName() + ".deleteComment Start!");

        MsgDTO dto = null;

        String msg = ""; // 메시지 내용

        try {
            String commentSeq = CmmUtil.nvl(request.getParameter("commentSeq"));

            log.info("commentSeq : " + commentSeq);

            CommentDTO pDTO = CommentDTO.builder()
                    .commentSeq(Long.parseLong(commentSeq))
                    .build();

            noticeJoinService.deleteComment(pDTO);

            msg = "삭제되었습니다.";

        } catch (Exception e) {
            msg = "실패하였습니다. : " + e.getMessage();
            log.info(e.toString());
            e.printStackTrace();

        } finally {
            // 결과 메시지 전달하기
            dto = MsgDTO.builder().msg(msg).build();

            log.info(this.getClass().getName() + ".deleteComment End!");

        }

        return dto;
    }


    @ResponseBody
    @PostMapping(value = "commentInsert")
    public MsgDTO commentInsert(HttpServletRequest request, HttpSession session) {

        log.info(this.getClass().getName() + ".commentInsert Start!");

        String msg = ""; // 메시지 내용

        MsgDTO dto = null; // 결과 메시지 구조

        try {
            // 로그인된 사용자 아이디를 가져오기
            // 로그인을 아직 구현하지 않았기에 공지사항 리스트에서 로그인 한 것처럼 Session 값을 저장함
            String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
            String contents = CmmUtil.nvl(request.getParameter("contents")); // 내용
            String nSeq = CmmUtil.nvl(request.getParameter("nSeq")); // 글번호

            /*
             * ####################################################################################
             * 반드시, 값을 받았으면, 꼭 로그를 찍어서 값이 제대로 들어오는지 파악해야함 반드시 작성할 것
             * ####################################################################################
             */
            log.info("session user_id : " + userId);
            log.info("contents : " + contents);
            log.info("nSeq : " + nSeq);

            // 데이터 저장하기 위해 DTO에 저장하기
            CommentDTO pDTO = CommentDTO.builder()
                    .userId(userId)
                    .contents(contents)
                    .noticeSeq(Long.parseLong(nSeq))
                    .build();

            /*
             * 게시글 등록하기위한 비즈니스 로직을 호출
             */
            noticeJoinService.insertComment(pDTO);

            // 저장이 완료되면 사용자에게 보여줄 메시지
            msg = "등록되었습니다.";

        } catch (Exception e) {

            // 저장이 실패되면 사용자에게 보여줄 메시지
            msg = "실패하였습니다. : " + e.getMessage();
            log.info(e.toString());
            e.printStackTrace();

        } finally {
            // 결과 메시지 전달하기
            dto = MsgDTO.builder().msg(msg).build();

            log.info(this.getClass().getName() + ".commentInsert End!");
        }

        return dto;
    }

}
