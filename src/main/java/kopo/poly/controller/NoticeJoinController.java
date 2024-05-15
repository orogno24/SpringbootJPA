package kopo.poly.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.*;
import kopo.poly.repository.entity.CommentKey;
import kopo.poly.service.INoticeJoinService;
import kopo.poly.service.INoticeService;
import kopo.poly.service.IUserInfoService;
import kopo.poly.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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
    private final IUserInfoService userInfoService;
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

    @GetMapping("/noticeFollowList")
    public String noticeFollowList(HttpSession session, ModelMap model) throws Exception {

        log.info(this.getClass().getName() + ".noticeFollowList Start!");

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        List<String> followUserIdList = Optional.ofNullable(userInfoService.noticeFollowList(userId))
                .orElseGet(ArrayList::new);

        log.info("followUserIdList : " + followUserIdList);

        List<NoticeDTO> rList = Optional.ofNullable(noticeJoinService.getFollowNoticeList(followUserIdList))
                .orElseGet(ArrayList::new);

        model.addAttribute("rList", rList);

        log.info(this.getClass().getName() + ".noticeFollowList End!");

        return "notice/noticeFollowList";
    }

    @GetMapping(value = "userNoticeList/{userId}")
    public String userNoticeList(@PathVariable("userId") String userId, HttpSession session, ModelMap model)
            throws Exception {

        log.info(this.getClass().getName() + ".userNoticeList Start!");

        UserInfoDTO rDTO = userInfoService.getUserInfo(userId);

        List<NoticeDTO> rList = Optional.ofNullable(noticeJoinService.getUserNoticeListUsingNativeQuery(userId))
                .orElseGet(ArrayList::new);

        model.addAttribute("userNoticeName", rDTO.userName());
        model.addAttribute("rList", rList);

        log.info(this.getClass().getName() + ".userNoticeList End!");

        return "notice/userNoticeList";
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
            String nSeq = CmmUtil.nvl(request.getParameter("nSeq"));

            log.info("commentSeq : " + commentSeq);
            log.info("nSeq : " + nSeq);

            CommentDTO pDTO = CommentDTO.builder()
                    .commentSeq(Long.parseLong(commentSeq))
                    .noticeSeq(Long.parseLong(nSeq))
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
    @Transactional
    @PostMapping(value = "insertComment")
    public MsgDTO insertComment(HttpServletRequest request, HttpSession session) {

        log.info(this.getClass().getName() + ".insertComment Start!");
        String msg = "";
        MsgDTO dto = null;

        try {
            String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
            String contents = CmmUtil.nvl(request.getParameter("contents")); // 내용
            String nSeq = CmmUtil.nvl(request.getParameter("nSeq")); // 글번호

            log.info("session user_id : " + userId);
            log.info("contents : " + contents);
            log.info("nSeq : " + nSeq);

            CommentDTO pDTO = CommentDTO.builder()
                    .noticeSeq(Long.valueOf(nSeq))
                    .userId(userId)
                    .contents(contents)
                    .build();

            noticeJoinService.insertComment(pDTO);

            msg = "등록되었습니다.";

        } catch (Exception e) {

            msg = "실패하였습니다. : " + e.getMessage();
            log.info(e.toString());
            e.printStackTrace();

        } finally {
            dto = MsgDTO.builder().msg(msg).build();

            log.info(this.getClass().getName() + ".insertComment End!");
        }

        return dto;
    }

}
