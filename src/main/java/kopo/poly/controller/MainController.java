package kopo.poly.controller;

import kopo.poly.dto.ApiDTO;
import kopo.poly.dto.EventDTO;
import kopo.poly.dto.UserInfoDTO;
import kopo.poly.service.IEventService;
import kopo.poly.service.IUserInfoService;
import kopo.poly.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {

    private final IEventService eventService;
    private final IUserInfoService userInfoService;

    @GetMapping("/main")
    public String main(HttpSession session, ModelMap model) throws Exception {

        log.info(this.getClass().getName() + ".main Start!");

        String userId = (String) session.getAttribute("SS_USER_ID");

        UserInfoDTO dto = userInfoService.getUserInfo(userId);

        model.addAttribute("userName", dto.userName());

        // 현재 날짜를 yyyy-MM-dd 포맷으로 가져오기
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        log.info("startDate : " + currentDate);
        log.info("endDate : " + currentDate);

        ApiDTO pDTO = ApiDTO.builder().
                startDate(currentDate).
                endDate(currentDate)
                .build();

        List<ApiDTO> rList = Optional.ofNullable(eventService.getTodayEventList(pDTO))
                .orElseGet(ArrayList::new);

        log.info("rList : " + rList);

        // 조회된 리스트 결과값 넣어주기
        model.addAttribute("rList", rList);

        log.info(this.getClass().getName() + ".main End!");

        return "main";
    }

    @GetMapping("/awsUpload")
    public String awsUpload() throws Exception {
        log.info(this.getClass().getName() + ".awsUpload 함수 실행");
        return "awsUpload";
    }

    @GetMapping("/alert")
    public String redirectPage(HttpServletRequest request, ModelMap modelMap, HttpSession session) throws Exception {
        log.info(this.getClass().getName() + ".alert 페이지 보여주는 함수 실행");

        String userName = (String) session.getAttribute("SS_USER_NAME");

        String msg = userName + "님 어서오세요!";

        modelMap.addAttribute("msg", msg);
        modelMap.addAttribute("url", "/main");

        return "/alert";
    }

    @GetMapping("/alert2")
    public String redirectPage2(HttpServletRequest request, ModelMap modelMap) throws Exception {
        log.info(this.getClass().getName() + ".alert2 페이지 보여주는 함수 실행");
        String msg = CmmUtil.nvl(request.getParameter("msg"), "로그인 페이지로 이동합니다.");
        modelMap.addAttribute("msg", msg);
        return "/alert2";
    }

    // 로딩 페이지
    @GetMapping("/redirect")
    public String intro() throws Exception {
        log.info(this.getClass().getName() + ".redirect 함수 실행");
        return "/redirect";
    }

    // 로딩 페이지
    @GetMapping("/redirect2")
    public String intro2() throws Exception {
        log.info(this.getClass().getName() + ".redirect2 함수 실행");
        return "redirect2";
    }

    // 로딩 페이지
    @GetMapping("/redirect3")
    public String intro3() throws Exception {
        log.info(this.getClass().getName() + ".redirect3 함수 실행");
        return "/redirect3";
    }

    // 로딩 페이지
    @GetMapping("/redirect4")
    public String intro4() throws Exception {
        log.info(this.getClass().getName() + ".redirect4 함수 실행");
        return "/redirect4";
    }

    // 로딩 페이지
    @GetMapping("/redirect5")
    public String intro5() throws Exception {
        log.info(this.getClass().getName() + ".redirect5 함수 실행");
        return "/redirect5";
    }

    // 로딩 페이지
    @GetMapping("/redirect6")
    public String intro6() throws Exception {
        log.info(this.getClass().getName() + ".redirect6 함수 실행");
        return "/redirect6";
    }

    // 로딩 페이지
    @GetMapping("/redirect7")
    public String intro7() throws Exception {
        log.info(this.getClass().getName() + ".redirect7 함수 실행");
        return "/redirect7";
    }

    // 로딩 페이지
    @GetMapping("/redirect8")
    public String intro8() throws Exception {
        log.info(this.getClass().getName() + ".redirect8 함수 실행");
        return "/redirect8";
    }

    // 로딩 페이지
    @GetMapping("/redirect9")
    public String intro9() throws Exception {
        log.info(this.getClass().getName() + ".redirect9 함수 실행");
        return "/redirect9";
    }

    // 로딩 페이지
    @GetMapping("/redirect10")
    public String intro10() throws Exception {
        log.info(this.getClass().getName() + ".redirect10 함수 실행");
        return "/redirect10";
    }

    // 로딩 페이지
    @GetMapping("/redirect11")
    public String intro11() throws Exception {
        log.info(this.getClass().getName() + ".redirect11 함수 실행");
        return "/redirect11";
    }

    // 가이드 페이지
    @GetMapping("/guide3")
    public String guide3(HttpSession session, ModelMap model) throws Exception {
        log.info(this.getClass().getName() + ".guide3 함수 실행");
        String userName = (String) session.getAttribute("SS_USER_NAME");
        model.addAttribute("userName", userName);
        return "/guide3";
    }

    @GetMapping("/guide5")
    public String guide5(HttpSession session, ModelMap model) throws Exception {
        log.info(this.getClass().getName() + ".guide5 함수 실행");
        String userName = (String) session.getAttribute("SS_USER_NAME");
        model.addAttribute("userName", userName);
        return "/guide5";
    }

    @GetMapping("/monitoring")
    public String monitoring(HttpSession session, ModelMap model) throws Exception {
        log.info(this.getClass().getName() + ".monitoring 함수 실행");
        String userName = (String) session.getAttribute("SS_USER_NAME");
        model.addAttribute("userName", userName);
        return "/monitoring";
    }

    @GetMapping("/map")
    public String map(HttpSession session, ModelMap model) throws Exception {
        log.info(this.getClass().getName() + ".map 함수 실행");
        String userName = (String) session.getAttribute("SS_USER_NAME");
        model.addAttribute("userName", userName);
        return "/map";
    }

    @PostMapping("/logout")
    public String handleLogout(HttpSession session) {
        session.invalidate(); // 세션 정보를 모두 삭제
        return "user/login"; // 로그인 페이지 또는 홈페이지로 리다이렉트
    }

}
