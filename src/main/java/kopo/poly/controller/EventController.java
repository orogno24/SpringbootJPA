package kopo.poly.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.EventDTO;
import kopo.poly.dto.MsgDTO;
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
    public String noticeInfo(HttpServletRequest request, ModelMap model) throws Exception {

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

    @ResponseBody
    @GetMapping(value = "filteredList")
    public List<EventDTO> filteredList(HttpSession session, ModelMap model, HttpServletRequest request)
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

        log.info(this.getClass().getName() + ".filteredList End!");

        return rList;
    }

    @PostMapping("/addBookmark")
    @ResponseBody
    public String addBookmark(HttpServletRequest request) {
        try {
            // HttpServletRequest를 통해 파라미터 값 추출
            String eventSeqParam = request.getParameter("eventSeq");
            long eventSeq = Long.parseLong(eventSeqParam);

            log.info("eventSeq : " + eventSeq);

            return "북마크에 추가되었습니다.";
        } catch (NumberFormatException e) {
            return "잘못된 번호 형식: " + e.getMessage();
        } catch (Exception e) {
            return "북마크 추가 실패: " + e.getMessage();
        }
    }

    @GetMapping(value = "getList")
    public List<EventDTO> getList(HttpServletRequest request)
            throws Exception {

        log.info(this.getClass().getName() + ".getList start!");

        List<EventDTO> rList = Optional.ofNullable(eventService.getList())
                .orElseGet(ArrayList::new);

        log.info(this.getClass().getName() + ".getList end!");

        return rList;
    }

}
