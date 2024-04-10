package kopo.poly.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.*;
import kopo.poly.dto.EventDTO;
import kopo.poly.service.IEventService;
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
@RequestMapping(value = "/event")
@RequiredArgsConstructor
@Controller
public class EventController {

    // @RequiredArgsConstructor 를 통해 메모리에 올라간 서비스 객체를 Controller에서 사용할 수 있게 주입함
    private final IEventService eventService;

    /**
     * 문화행사 리스트 보여주기
     * <p>
     * GetMapping(value = "event/eventList") =>  GET방식을 통해 접속되는 URL이 event/eventList 경우 아래 함수를 실행함
     */
    @GetMapping(value = "eventList")
    public String eventList(HttpSession session, ModelMap model)
            throws Exception {

        // 로그 찍기(추후 찍은 로그를 통해 이 함수에 접근했는지 파악하기 용이하다.)
        log.info(this.getClass().getName() + ".eventList Start!");

        List<EventDTO> rList = Optional.ofNullable(eventService.getEventList())
                .orElseGet(ArrayList::new);

        // 조회된 리스트 결과값 넣어주기
        model.addAttribute("rList", rList);

        // 로그 찍기(추후 찍은 로그를 통해 이 함수 호출이 끝났는지 파악하기 용이하다.)
        log.info(this.getClass().getName() + ".eventList End!");

        // 함수 처리가 끝나고 보여줄 HTML (Thymeleaf) 파일명
        // templates/event/eventList.html
        return "event/eventList";
    }

    /**
     * 문화행사 상세보기
     */
    @GetMapping(value = "eventInfo")
    public String eventInfo(HttpServletRequest request, ModelMap model) throws Exception {

        log.info(this.getClass().getName() + ".eventInfo Start!");

        String nSeq = CmmUtil.nvl(request.getParameter("nSeq"), "0");

        log.info("nSeq : " + nSeq);

        EventDTO pDTO = EventDTO.builder().eventSeq(Long.parseLong(nSeq)).build();

        EventDTO rDTO = Optional.ofNullable(eventService.getEventInfo(pDTO, true))
                .orElseGet(() -> EventDTO.builder().build());

        // 조회된 리스트 결과값 넣어주기
        model.addAttribute("rDTO", rDTO);

        log.info(this.getClass().getName() + ".eventInfo End!");

        return "event/eventInfo";
    }

    @GetMapping(value = "eventSearch")
    public String eventSearch(HttpSession session, ModelMap model)
            throws Exception {

        log.info(this.getClass().getName() + ".eventSearch Start!");

        log.info(this.getClass().getName() + ".eventSearch End!");

        return "event/eventSearch";
    }

    @GetMapping(value = "apiSearch")
    public String apiSearch(HttpSession session, ModelMap model)
            throws Exception {

        log.info(this.getClass().getName() + ".apiSearch Start!");

        log.info(this.getClass().getName() + ".apiSearch End!");

        return "event/apiSearch";
    }

    @ResponseBody
    @GetMapping(value = "filteredList")
    public List<EventDTO> filteredList(HttpServletRequest request)
            throws Exception {

        // 로그 찍기(추후 찍은 로그를 통해 이 함수에 접근했는지 파악하기 용이하다.)
        log.info(this.getClass().getName() + ".filteredList Start!");

        String eventPlace = CmmUtil.nvl(request.getParameter("region"));
        String eventSort = CmmUtil.nvl(request.getParameter("tourType"));
        String eventDate = CmmUtil.nvl(request.getParameter("tourDate"));

        log.info("eventPlace : " + eventPlace);
        log.info("eventSort : " + eventSort);
        log.info("eventDate : " + eventDate);

        EventDTO pDTO = EventDTO.builder().
                eventPlace(eventPlace).
                eventSort(eventSort).
                eventDate(eventDate)
                .build();

        List<EventDTO> rList = Optional.ofNullable(eventService.getEventListSearch(pDTO))
                .orElseGet(ArrayList::new);

        log.info("rList : " + rList);

        log.info(this.getClass().getName() + ".filteredList End!");

        return rList;
    }

    @ResponseBody
    @GetMapping(value = "getList")
    public List<ApiDTO> getList(HttpServletRequest request)
            throws Exception {

        log.info(this.getClass().getName() + ".getList start!");

        String guName = CmmUtil.nvl(request.getParameter("guName"));
        String codename = CmmUtil.nvl(request.getParameter("codename"));
        String themeCode = CmmUtil.nvl(request.getParameter("themeCode"));
        String isFree = CmmUtil.nvl(request.getParameter("isFree"));
        String startDate = CmmUtil.nvl(request.getParameter("startDate"));
        String endDate = CmmUtil.nvl(request.getParameter("endDate"));

        log.info("guName : " + guName);
        log.info("codename : " + codename);
        log.info("themeCode : " + themeCode);
        log.info("isFree : " + isFree);
        log.info("startDate : " + startDate);
        log.info("endDate : " + endDate);

        ApiDTO pDTO = ApiDTO.builder().
                guName(guName).
                codename(codename).
                themeCode(themeCode).
                isFree(isFree).
                startDate(startDate).
                endDate(endDate)
                .build();

        List<ApiDTO> rList = Optional.ofNullable(eventService.getList(pDTO))
                .orElseGet(ArrayList::new);

        log.info(this.getClass().getName() + ".getList end!");

        return rList;
    }

    @GetMapping(value = "apiInfo")
    public String apiInfo(HttpServletRequest request, ModelMap model, HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".apiInfo Start!");

        // 고유 식별자를 받는 방식에 따라 변경 필요
        String uniqueIdentifier = CmmUtil.nvl(request.getParameter("nSeq"), "");
        String userId = (String) session.getAttribute("SS_USER_ID");
        String eventSeq = uniqueIdentifier;

        log.info("Unique Identifier: " + uniqueIdentifier);
        log.info("userId: " + userId);
        log.info("eventSeq: " + eventSeq);

        uniqueIdentifier = "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=" + uniqueIdentifier + "&thumb=Y";

        ApiDTO rDTO = Optional.ofNullable(eventService.getApiInfo(uniqueIdentifier))
                .orElseGet(() -> ApiDTO.builder().build());

        BookmarkDTO gDTO = BookmarkDTO.builder().userId(userId).eventSeq(eventSeq).build();

        BookmarkDTO hDTO = Optional.ofNullable(eventService.getBookmarkExists(gDTO))
                .orElseGet(() -> BookmarkDTO.builder().build());

        // 조회된 결과값 넣어주기
        model.addAttribute("rDTO", rDTO);
        model.addAttribute("hDTO", hDTO);

        log.info(this.getClass().getName() + ".apiInfo End!");

        return "event/apiInfo";
    }

    @GetMapping(value = "eventBookmarkList")
    public String eventBookmarkList(HttpSession session, ModelMap model)
            throws Exception {

        // 로그 찍기(추후 찍은 로그를 통해 이 함수에 접근했는지 파악하기 용이하다.)
        log.info(this.getClass().getName() + ".eventBookmarkList Start!");

        List<EventDTO> rList = Optional.ofNullable(eventService.getEventList())
                .orElseGet(ArrayList::new);

        // 조회된 리스트 결과값 넣어주기
        model.addAttribute("rList", rList);

        // 로그 찍기(추후 찍은 로그를 통해 이 함수 호출이 끝났는지 파악하기 용이하다.)
        log.info(this.getClass().getName() + ".eventBookmarkList End!");

        // 함수 처리가 끝나고 보여줄 HTML (Thymeleaf) 파일명
        // templates/event/eventList.html
        return "event/eventList";
    }

    @GetMapping("/eventCalendar")
    public String eventCalendar(HttpSession session, ModelMap model) throws Exception {

        log.info(this.getClass().getName() + ".eventCalendar Start!");

        log.info(this.getClass().getName() + ".eventCalendar End!");

        return "event/eventCalendar";
    }

    @ResponseBody
    @GetMapping("/getCalendarDate")
    public List<BookmarkDTO> getCalendarDate(HttpSession session, ModelMap model) throws Exception {

        log.info(this.getClass().getName() + ".getCalendarDate Start!");

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        BookmarkDTO pDTO = BookmarkDTO.builder().userId(userId).build();

        List<BookmarkDTO> rList = Optional.ofNullable(eventService.getBookmarkDate(pDTO))
                .orElseGet(ArrayList::new);

        log.info("rList : " + rList);

        log.info(this.getClass().getName() + ".getCalendarDate End!");

        return rList;
    }

    @ResponseBody
    @PostMapping("/addBookmark")
    public MsgDTO addBookmark(HttpServletRequest request, HttpSession session) {

        log.info(this.getClass().getName() + ".addBookmark Start!");


        MsgDTO dto = null;
        String msg = "";

        try {
            String userId = (String) session.getAttribute("SS_USER_ID");
            String eventSeq = CmmUtil.nvl(request.getParameter("eventSeq"));
            String eventTitle = CmmUtil.nvl(request.getParameter("title"));
            String startDate = CmmUtil.nvl(request.getParameter("startDate"));
            String endDate = CmmUtil.nvl(request.getParameter("endDate"));

            log.info("userId : " + userId);
            log.info("eventSeq : " + eventSeq);
            log.info("eventTitle : " + eventTitle);
            log.info("startDate : " + startDate);
            log.info("endDate : " + endDate);

            BookmarkDTO pDTO = BookmarkDTO.builder()
                    .userId(userId)
                    .eventSeq(eventSeq)
                    .eventTitle(eventTitle)
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();

            eventService.insertBookmark(pDTO);

            msg = "북마크에 추가되었습니다.";

        } catch (Exception e) {

            msg = "실패하였습니다. : " + e.getMessage();
            log.info(e.toString());
            e.printStackTrace();

        } finally {

            dto = MsgDTO.builder().msg(msg).build();

            log.info(this.getClass().getName() + ".addBookmark End!");

        }

        return dto;

    }

    @ResponseBody
    @PostMapping(value = "removeBookmark")
    public MsgDTO removeBookmark(HttpServletRequest request, HttpSession session) {

        log.info(this.getClass().getName() + ".removeBookmark Start!");

        String msg = ""; // 메시지 내용
        MsgDTO dto = null; // 결과 메시지 구조

        try {
            String userId = (String) session.getAttribute("SS_USER_ID");
            String eventSeq = CmmUtil.nvl(request.getParameter("eventSeq"));

            log.info("userId : " + userId);
            log.info("eventSeq : " + eventSeq);

            /*
             * 값 전달은 반드시 DTO 객체를 이용해서 처리함 전달 받은 값을 DTO 객체에 넣는다.
             */
            BookmarkDTO pDTO = BookmarkDTO.builder().userId(userId).eventSeq(eventSeq).build();

            // 게시글 삭제하기 DB
            eventService.removeBookmark(pDTO);

            msg = "삭제되었습니다.";

        } catch (Exception e) {
            msg = "실패하였습니다. : " + e.getMessage();
            log.info(e.toString());
            e.printStackTrace();

        } finally {

            // 결과 메시지 전달하기
            dto = MsgDTO.builder().msg(msg).build();

            log.info(this.getClass().getName() + ".removeBookmark End!");

        }

        return dto;
    }


}
