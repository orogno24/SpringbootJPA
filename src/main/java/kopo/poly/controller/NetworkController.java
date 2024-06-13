package kopo.poly.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.*;
import kopo.poly.service.*;
import kopo.poly.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "/network")
@RequiredArgsConstructor
public class NetworkController {

    private final INetworkService networkService;
    private final IEventService eventService;
    private final IChatService chatService;
    private final IUserInfoService userInfoService;
    private final ICultureService cultureService;

    /**
     * 일정 추가 페이지
     */
    @GetMapping("/createNetwork")
    public String createNetwork(@RequestParam(required = false) String eventName,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate,
                                @RequestParam(required = false) String selectedEventId,
                                @RequestParam(required = false) String selectedEventName,
                                @RequestParam(required = false) String ImagePath,
                                Model model) throws Exception {
        log.info(this.getClass().getName() + ".createNetwork 함수 실행");

        model.addAttribute("eventName", eventName);
        model.addAttribute("description", description);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedEventId", selectedEventId);
        model.addAttribute("selectedEventName", selectedEventName);
        model.addAttribute("ImagePath", ImagePath);

        return "network/createNetwork";
    }

    /**
     * 일정 추가 페이지
     */
    @PostMapping("/createNetwork")
    public String createNetwork(@RequestParam String eventName,
                              @RequestParam String description,
                              @RequestParam String startDate,
                              @RequestParam String endDate,
                              @RequestParam(required = false) String selectedEventId,
                              @RequestParam(required = false) String selectedEventName,
                              @RequestParam(required = false) String ImagePath,
                              Model model, HttpSession session) {

        log.info(this.getClass().getName() + ".createNetwork 함수 실행");

        model.addAttribute("eventName", eventName);
        model.addAttribute("description", description);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedEventId", selectedEventId);
        model.addAttribute("selectedEventName", selectedEventName);
        model.addAttribute("ImagePath", ImagePath);

        return "network/createNetwork";
    }

    /**
     * 일정 추가
     */
    @ResponseBody
    @Transactional
    @PostMapping("/insertNetwork")
    public MsgDTO insertNetwork(@RequestParam String eventName,
                                @RequestParam String description,
                                @RequestParam String startDate,
                                @RequestParam String endDate,
                                @RequestParam(required = false) String selectedEventId,
                                @RequestParam(required = false) String selectedEventName,
                                @RequestParam(required = false) String ImagePath,
                                HttpSession session) {

        log.info(this.getClass().getName() + ".insertNetwork 함수 실행");
        String msg = "";
        MsgDTO dto = null;

        try {
            String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
            String selectedStartDate = CmmUtil.nvl(startDate);
            String selectedEndDate = CmmUtil.nvl(endDate);

            NetworkDTO pDTO = NetworkDTO.builder()
                    .userId(userId)
                    .name(eventName)
                    .contents(description)
                    .startDate(selectedStartDate)
                    .endDate(selectedEndDate)
                    .eventSeq(selectedEventId)
                    .eventName(selectedEventName)
                    .imagePath(ImagePath)
                    .build();

            networkService.insertNetWorkInfo(pDTO);
            msg = "등록되었습니다.";
        } catch (Exception e) {
            msg = "실패하였습니다. : " + e.getMessage();
            log.info(e.toString());
            e.printStackTrace();
        } finally {
            dto = MsgDTO.builder().msg(msg).build();
            log.info(this.getClass().getName() + ".insertNetwork End!");
        }

        return dto;
    }

    /**
     * 일정 리스트 조회 페이지
     */
    @GetMapping(value = "networkList")
    public String networkList(ModelMap model) throws Exception {

        log.info(this.getClass().getName() + ".networkList Start!");

        String event = "EVENT";
        String culture = "CULTURE";

        List<NetworkDTO> eventList = Optional.ofNullable(networkService.getNetworkList(event))
                .orElseGet(ArrayList::new);

        List<NetworkDTO> cultureList = Optional.ofNullable(networkService.getNetworkList(culture))
                .orElseGet(ArrayList::new);

        model.addAttribute("eventList", eventList);
        model.addAttribute("cultureList", cultureList);

        log.info(this.getClass().getName() + ".networkList End!");

        return "network/networkList";
    }

    /**
     * 일정 상세보기(문화행사 일정)
     */
    @Transactional
    @GetMapping(value = "networkEventInfo")
    public String networkEventInfo(HttpServletRequest request, ModelMap model, HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".networkEventInfo Start!");

        String uniqueIdentifier = CmmUtil.nvl(request.getParameter("nSeq"), "");
        String userId = (String) session.getAttribute("SS_USER_ID");

        uniqueIdentifier = "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=" + uniqueIdentifier + "&thumb=Y";

        RedisDTO redisDTO = null;
        String colNm = "EVENT_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        redisDTO = eventService.getCulturalEvents(colNm);

        ApiDTO rDTO = Optional.ofNullable(eventService.getApiInfo(redisDTO, uniqueIdentifier))
                .orElseGet(() -> ApiDTO.builder().build());

        String networkSeq = CmmUtil.nvl(request.getParameter("networkSeq"), "");
        NetworkDTO nDTO = networkService.getNetworkInfo(networkSeq);
        String roomName = "커뮤니티 채팅방 " + networkSeq;

        if (chatService.findByRoomName(roomName)) {
            log.info("exists");
        } else {
            chatService.insertRoomName(roomName, userId);
            log.info("success");
        }

        ScheduleDTO gDTO = ScheduleDTO.builder().userId(userId).eventSeq(networkSeq).build();

        ScheduleDTO hDTO = Optional.ofNullable(networkService.getBookmarkExists(gDTO))
                .orElseGet(() -> ScheduleDTO.builder().build());

        UserInfoDTO dto = userInfoService.getUserInfo(nDTO.userId());

        log.info("hDTO existsYn : " + hDTO.existsYn());

        // 조회된 결과값 넣어주기
        model.addAttribute("rDTO", rDTO);
        model.addAttribute("hDTO", hDTO);
        model.addAttribute("nDTO", nDTO);
        model.addAttribute("dto", dto);
        model.addAttribute("userId", userId);
        model.addAttribute("roomName", roomName);

        log.info(this.getClass().getName() + ".networkEventInfo End!");

        return "network/networkEventInfo";
    }

    /**
     * 일정 상세보기(문화시설 일정)
     */
    @GetMapping(value = "networkCultureInfo")
    public String networkCultureInfo(HttpServletRequest request, ModelMap model, HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".networkCultureInfo Start!");

        String nSeq = CmmUtil.nvl(request.getParameter("nSeq"), "");
        String userId = (String) session.getAttribute("SS_USER_ID");

        log.info("nSeq: " + nSeq);
        log.info("userId: " + userId);

        CultureDTO rDTO = Optional.ofNullable(cultureService.getCultureInfo(nSeq))
                .orElseGet(() -> CultureDTO.builder().build());

        String networkSeq = CmmUtil.nvl(request.getParameter("networkSeq"), "");

        ScheduleDTO gDTO = ScheduleDTO.builder().userId(userId).eventSeq(networkSeq).build();

        ScheduleDTO hDTO = Optional.ofNullable(networkService.getBookmarkExists(gDTO))
                .orElseGet(() -> ScheduleDTO.builder().build());

        NetworkDTO nDTO = networkService.getNetworkInfo(networkSeq);
        UserInfoDTO dto = userInfoService.getUserInfo(nDTO.userId());

        String roomName = "커뮤니티 채팅방 " + networkSeq;

        if (chatService.findByRoomName(roomName)) {
            log.info("exists");
        } else {
            chatService.insertRoomName(roomName, userId);
            log.info("success");
        }

        // 조회된 결과값 넣어주기
        model.addAttribute("rDTO", rDTO);
        model.addAttribute("hDTO", hDTO);
        model.addAttribute("nDTO", nDTO);
        model.addAttribute("dto", dto);
        model.addAttribute("userId", userId);
        model.addAttribute("roomName", roomName);

        log.info(this.getClass().getName() + ".networkCultureInfo End!");

        return "network/networkCultureInfo";
    }

    /**
     * 일정 북마크 추가
     */
    @ResponseBody
    @PostMapping("/addNetwork")
    public MsgDTO addNetwork(HttpServletRequest request, HttpSession session) {

        log.info(this.getClass().getName() + ".addNetwork Start!");

        MsgDTO dto = null;
        String msg = "";

        try {
            String userId = (String) session.getAttribute("SS_USER_ID");
            String eventSeq = CmmUtil.nvl(request.getParameter("eventSeq"));

            log.info("userId : " + userId);
            log.info("eventSeq : " + eventSeq);

            ScheduleDTO pDTO = ScheduleDTO.builder()
                    .userId(userId)
                    .eventSeq(eventSeq)
                    .build();

            networkService.insertBookmark(pDTO);

            msg = "북마크에 추가되었습니다.";

        } catch (Exception e) {

            msg = "실패하였습니다. : " + e.getMessage();
            log.info(e.toString());
            e.printStackTrace();

        } finally {

            dto = MsgDTO.builder().msg(msg).build();

            log.info(this.getClass().getName() + ".addNetwork End!");

        }

        return dto;

    }

    /**
     * 일정 북마크 해제
     */
    @ResponseBody
    @PostMapping(value = "removeNetwork")
    public MsgDTO removeNetwork(HttpServletRequest request, HttpSession session) {

        log.info(this.getClass().getName() + ".removeNetwork Start!");

        String msg = ""; // 메시지 내용
        MsgDTO dto = null; // 결과 메시지 구조

        try {
            String userId = (String) session.getAttribute("SS_USER_ID");
            String eventSeq = CmmUtil.nvl(request.getParameter("eventSeq"));

            log.info("userId : " + userId);
            log.info("eventSeq : " + eventSeq);

            ScheduleDTO pDTO = ScheduleDTO.builder().userId(userId).eventSeq(eventSeq).build();

            networkService.removeBookmark(pDTO);

            msg = "삭제되었습니다.";

        } catch (Exception e) {
            msg = "실패하였습니다. : " + e.getMessage();
            log.info(e.toString());
            e.printStackTrace();

        } finally {

            // 결과 메시지 전달하기
            dto = MsgDTO.builder().msg(msg).build();

            log.info(this.getClass().getName() + ".removeNetwork End!");

        }

        return dto;
    }

    /**
     * 달력에 일정 추가
     */
    @ResponseBody
    @GetMapping("/getScheduleDateList")
    public List<NetworkDTO> getScheduleDateList(@RequestParam("userId") String userId) throws Exception {

        log.info(this.getClass().getName() + ".getScheduleDateList Start!");

        log.info("userId : " + userId);

        ScheduleDTO pDTO = ScheduleDTO.builder().userId(userId).build();

        List<ScheduleDTO> rList = Optional.ofNullable(networkService.getScheduleSeq(pDTO))
                .orElseGet(ArrayList::new);

        List<String> ScheduleSeqList = rList.stream()
                .map(ScheduleDTO::eventSeq)
                .collect(Collectors.toList());

        log.info("rList : " + rList);
        log.info("ScheduleSeqList : " + ScheduleSeqList);

        List<NetworkDTO> networkList = networkService.getAllNetworkList();

        List<NetworkDTO> eventDetails = Optional.ofNullable(networkService.getNetworkDateList(networkList, ScheduleSeqList))
                .orElseGet(ArrayList::new);

        log.info("eventDetails : " + eventDetails);

        log.info(this.getClass().getName() + ".getScheduleDateList End!");

        return eventDetails;
    }

}
