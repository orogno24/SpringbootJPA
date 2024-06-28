package kopo.poly.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.ApiDTO;
import kopo.poly.dto.RedisDTO;
import kopo.poly.dto.UserInfoDTO;
import kopo.poly.dto.UserInterestsDTO;
import kopo.poly.service.IEventService;
import kopo.poly.service.IRecommendationService;
import kopo.poly.service.IUserInfoService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.NetworkUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {

    private final IEventService eventService;
    private final IUserInfoService userInfoService;
    private final IRecommendationService recommendationService;

    /**
     * 메인 페이지
     */
    @GetMapping("/main")
    public String main(HttpSession session, ModelMap model) throws Exception {

        log.info(this.getClass().getName() + ".main Start!");

        String userId = (String) session.getAttribute("SS_USER_ID");
        UserInfoDTO dto = userInfoService.getUserInfo(userId);

        RedisDTO redisDTO = null;
        String colNm = "EVENT_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        redisDTO = eventService.getCulturalEvents(colNm);

        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        YearMonth currentYearMonth = YearMonth.now();
        String startDate = currentYearMonth.atDay(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDate = currentYearMonth.atEndOfMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        ApiDTO pDTO = ApiDTO.builder().startDate(currentDate).endDate(currentDate).build();
        ApiDTO pDTO2 = ApiDTO.builder().startDate(startDate).endDate(endDate).build();

        List<ApiDTO> rList = Optional.ofNullable(eventService.getTodayEventList(redisDTO, pDTO))
                .orElseGet(ArrayList::new);
        Map<String, Long> topDistricts = eventService.getEventCountList(redisDTO, pDTO2);
        Map<String, Long> eventTypeCount = eventService.getEventTypeCountList(redisDTO, pDTO2);

        String currentMonth = String.valueOf(currentYearMonth.getMonthValue());

        model.addAttribute("dto", dto);
        model.addAttribute("rList", rList);
        model.addAttribute("topDistricts", new ObjectMapper().writeValueAsString(topDistricts));
        model.addAttribute("eventTypeCount", new ObjectMapper().writeValueAsString(eventTypeCount));
        model.addAttribute("currentMonth", currentMonth);

        log.info(this.getClass().getName() + ".main End!");

        return "main";
    }

    /**
     * 로딩 페이지 (인덱스)
     */
    @GetMapping("/index")
    public String index() throws Exception {
        log.info(this.getClass().getName() + ".index 함수 실행");
        return "index";
    }

    @GetMapping("/test")
    public String test() throws Exception {
        log.info(this.getClass().getName() + ".test 함수 실행");
        return "test";
    }

    @ResponseBody
    @GetMapping("/testList")
    public List<ApiDTO> testList(HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".testList 함수 실행");

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        RedisDTO redisDTO = null;
        String colNm = "EVENT_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        redisDTO = eventService.getCulturalEvents(colNm);

        ApiDTO pDTO = ApiDTO.builder().build();

        List<UserInterestsDTO> keywords = userInfoService.getKeywordList(userId);

        // keywords 리스트에서 keyword 항목만 추출하여 List<String> 타입으로 변환
        List<String> interestKeywords = keywords.stream()
                .map(UserInterestsDTO::keyword) // UserInterestsDTO 객체에서 keyword 항목을 추출
                .collect(Collectors.toList());

        log.info("interestKeywords : " + interestKeywords);

        List<ApiDTO> rList = Optional.ofNullable(recommendationService.getRecommendedEvents(redisDTO, pDTO, interestKeywords))
                .orElseGet(ArrayList::new);

        for (ApiDTO apiDTO : rList) {
            log.info("ApiDTO: " + apiDTO.toString());
        }

        return rList;
    }

    @GetMapping("/alert")
    public String redirectPage(HttpServletRequest request, ModelMap modelMap, HttpSession session) throws Exception {
        log.info(this.getClass().getName() + ".alert 페이지 보여주는 함수 실행");

        String userName = (String) session.getAttribute("SS_USER_NAME");

        String msg = userName + "님 어서오세요!";

        modelMap.addAttribute("msg", msg);
        modelMap.addAttribute("url", "/main");

        return "alert";
    }

}
