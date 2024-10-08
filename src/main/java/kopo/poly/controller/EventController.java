package kopo.poly.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.*;
import kopo.poly.service.IEventService;
import kopo.poly.service.IRecommendationService;
import kopo.poly.service.IUserInfoService;
import kopo.poly.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
@RequestMapping(value = "/event")
@RequiredArgsConstructor
@Controller
public class EventController {

    // @RequiredArgsConstructor 를 통해 메모리에 올라간 서비스 객체를 Controller에서 사용할 수 있게 주입함
    private final IEventService eventService;
    private final IUserInfoService userInfoService;
    private final IRecommendationService recommendationService;

    /**
     * 문화행사 검색 페이지
     */
    @GetMapping(value = "apiSearch")
    public String apiSearch(HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".apiSearch Start!");

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        if (userId.length() > 0) {
            return "event/apiSearch";
        } else {
            return "redirect:/user/login";
        }

    }

    /**
     * 추천 문화행사 리스트
     */
    @ResponseBody
    @GetMapping("/recommendList")
    public List<ApiDTO> recommendList(HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".recommendList 함수 실행");

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        RedisDTO redisDTO;
        String colNm = "EVENT_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        redisDTO = eventService.getCulturalEvents(colNm);

        ApiDTO pDTO = ApiDTO.builder().build();

        List<UserInterestsDTO> keywords = userInfoService.getKeywordList(userId);

        // keywords 리스트에서 keyword 항목만 추출하여 List<String> 타입으로 변환
        List<String> interestKeywords = keywords.stream()
                .map(UserInterestsDTO::keyword) // UserInterestsDTO 객체에서 keyword 항목을 추출
                .collect(Collectors.toList());

        List<ApiDTO> rList = Optional.ofNullable(recommendationService.getRecommendedEvents(redisDTO, pDTO, interestKeywords))
                .orElseGet(ArrayList::new);

        for (ApiDTO apiDTO : rList) {
            log.info("ApiDTO: " + apiDTO.toString());
        }

        return rList;
    }

    /**
     * 일정에 문화행사 추가
     */
    @GetMapping(value = "selectApi")
    public String selectApi(HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".selectApi Start!");

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        if (userId.length() > 0) {
            return "event/selectApi";
        } else {
            return "redirect:/user/login";
        }

    }

    /**
     * 그래프를 위한 구별 문화행사 타입 개수 조회
     */
    @ResponseBody
    @GetMapping(value = "getList")
    public List<ApiDTO> getList(HttpServletRequest request)
            throws Exception {

        log.info(this.getClass().getName() + ".getList start!");

        RedisDTO redisDTO = null;
        String colNm = "EVENT_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        redisDTO = eventService.getCulturalEvents(colNm);

        String guName = CmmUtil.nvl(request.getParameter("guName"));
        String codename = CmmUtil.nvl(request.getParameter("codename"));
        String themeCode = CmmUtil.nvl(request.getParameter("themeCode"));
        String isFree = CmmUtil.nvl(request.getParameter("isFree"));
        String startDate = CmmUtil.nvl(request.getParameter("startDate"));
        String endDate = CmmUtil.nvl(request.getParameter("endDate"));
        String searchKeyword = CmmUtil.nvl(request.getParameter("searchKeyword"));

        log.info("guName : " + guName);
        log.info("codename : " + codename);
        log.info("themeCode : " + themeCode);
        log.info("isFree : " + isFree);
        log.info("startDate : " + startDate);
        log.info("endDate : " + endDate);
        log.info("searchKeyword : " + searchKeyword);

        ApiDTO pDTO = ApiDTO.builder().
                guName(guName).
                codename(codename).
                themeCode(themeCode).
                isFree(isFree).
                startDate(startDate).
                endDate(endDate).
                searchKeyword(searchKeyword)
                .build();

        List<ApiDTO> rList = Optional.ofNullable(eventService.getList(redisDTO, pDTO))
                .orElseGet(ArrayList::new);

        log.info(this.getClass().getName() + ".getList end!");

        return rList;
    }

    /**
     * 문화행사 상세보기 페이지
     */
    @GetMapping(value = "apiInfo")
    public String apiInfo(HttpServletRequest request, ModelMap model, HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".apiInfo Start!");

        String uniqueIdentifier = CmmUtil.nvl(request.getParameter("nSeq"), "");
        String userId = (String) session.getAttribute("SS_USER_ID");
        String eventSeq = uniqueIdentifier;

        log.info("Unique Identifier: " + uniqueIdentifier);
        log.info("userId: " + userId);
        log.info("eventSeq: " + eventSeq);

        if (userId.length() > 0) {

            uniqueIdentifier = "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=" + uniqueIdentifier + "&thumb=Y";

            RedisDTO redisDTO = null;
            String colNm = "EVENT_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            redisDTO = eventService.getCulturalEvents(colNm);

            ApiDTO rDTO = Optional.ofNullable(eventService.getApiInfo(redisDTO, uniqueIdentifier))
                    .orElseGet(() -> ApiDTO.builder().build());

            BookmarkDTO gDTO = BookmarkDTO.builder().userId(userId).eventSeq(eventSeq).build();

            BookmarkDTO hDTO = Optional.ofNullable(eventService.getBookmarkExists(gDTO))
                    .orElseGet(() -> BookmarkDTO.builder().build());

            // 조회된 결과값 넣어주기
            model.addAttribute("rDTO", rDTO);
            model.addAttribute("hDTO", hDTO);
            model.addAttribute("userId", userId);

            return "event/apiInfo";

        } else {

            return "redirect:/user/login";

        }


    }

    /**
     * 북마크한 일정 조회 페이지
     */
    @GetMapping("/eventCalendarList/{userId}")
    public String eventCalendarList(@PathVariable("userId") String userId, ModelMap model) throws Exception {

        log.info(this.getClass().getName() + ".eventCalendarList Start!");

        UserInfoDTO dto = userInfoService.getUserInfo(userId);

        model.addAttribute("dto", dto);

        log.info(this.getClass().getName() + ".eventCalendarList End!");

        return "event/eventCalendarList";
    }

    /**
     * 달력에 문화행사 일정 추가
     */
    @ResponseBody
    @GetMapping("/getCalendarDateList")
    public List<BookmarkDTO> getCalendarDateList(@RequestParam("userId") String userId, HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".getCalendarDateList Start!");

        log.info("userId : " + userId);

        BookmarkDTO pDTO = BookmarkDTO.builder().userId(userId).build();

        List<BookmarkDTO> rList = Optional.ofNullable(eventService.getBookmarkSeq(pDTO)) // 해당 유저아이디의 전체 북마크 데이터 불러옴
                .orElseGet(ArrayList::new);

        List<String> BookMarkSeqList = rList.stream() // 북마크 데이터에서 eventSeq들만 추출하는 과정
                .map(BookmarkDTO::eventSeq)
                .collect(Collectors.toList());

        log.info("rList : " + rList);
        log.info("eventIds : " + BookMarkSeqList);

        RedisDTO redisDTO = null;
        String colNm = "EVENT_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        redisDTO = eventService.getCulturalEvents(colNm);

        List<BookmarkDTO> eventDetails = Optional.ofNullable(eventService.getBookmarkDateList(redisDTO, BookMarkSeqList)) // 추출한 eventSeq 리스트를 요청인자로 해서 전체 문화행사 데이터에서 북마크된것만 필터링함
                .orElseGet(ArrayList::new);

        log.info("eventDetails : " + eventDetails);

        log.info(this.getClass().getName() + ".getCalendarDateList End!");

        return eventDetails;
    }

    /**
     * 북마크 추가
     */
    @ResponseBody
    @PostMapping("/addBookmark")
    public MsgDTO addBookmark(HttpServletRequest request, HttpSession session) {

        log.info(this.getClass().getName() + ".addBookmark Start!");

        MsgDTO dto = null;
        String msg = "";

        try {
            String userId = (String) session.getAttribute("SS_USER_ID");
            String eventSeq = CmmUtil.nvl(request.getParameter("eventSeq"));

            log.info("userId : " + userId);
            log.info("eventSeq : " + eventSeq);

            BookmarkDTO pDTO = BookmarkDTO.builder()
                    .userId(userId)
                    .eventSeq(eventSeq)
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

    /**
     * 북마크 해제
     */
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
