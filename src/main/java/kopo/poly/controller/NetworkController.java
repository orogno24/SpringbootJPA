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

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @PostMapping("/createNetwork")
    public String showCreateNetworkForm(@RequestBody NetworkDTO pDTO, Model model) {
        log.info(this.getClass().getName() + ".createNetwork 함수 실행");
        log.info("pDTO: " + pDTO);

        String selectedEventId = pDTO.eventSeq();
        if (selectedEventId == null) return "redirect:/user/login";

        model.addAttribute("eventName", pDTO.name());
        model.addAttribute("description", pDTO.contents());
        model.addAttribute("startDate", pDTO.startDate());
        model.addAttribute("endDate", pDTO.endDate());
        model.addAttribute("selectedEventId", selectedEventId);
        model.addAttribute("selectedEventName", pDTO.eventName());
        model.addAttribute("ImagePath", pDTO.imagePath());

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
                                @RequestParam Long userCount,
                                @RequestParam(required = false) String selectedEventId,
                                @RequestParam(required = false) String selectedEventName,
                                @RequestParam(required = false) String ImagePath,
                                HttpSession session) {

        log.info(this.getClass().getName() + ".insertNetwork 함수 실행");
        String msg = "";
        MsgDTO dto;

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
                    .userCount(userCount)
                    .eventSeq(selectedEventId)
                    .eventName(selectedEventName)
                    .imagePath(ImagePath)
                    .build();

            Long networkSeq = networkService.insertNetWorkInfo(pDTO);

            ScheduleDTO scheduleDTO = ScheduleDTO.builder()
                    .userId(userId)
                    .networkSeq(networkSeq)
                    .build();
            networkService.insertBookmark(scheduleDTO);
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
    public String networkList(ModelMap model, HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".networkList Start!");

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        if (userId.length() > 0) {
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
        } else {
            return "redirect:/user/login";
        }

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

        if (userId.length() > 0) {

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
                chatService.insertRoomNameWithSeq(roomName, userId, Long.valueOf(networkSeq));
                log.info("success");
            }

            ScheduleDTO gDTO = ScheduleDTO.builder().userId(userId).networkSeq(Long.valueOf(networkSeq)).build();

            ScheduleDTO hDTO = Optional.ofNullable(networkService.getBookmarkExists(gDTO))
                    .orElseGet(() -> ScheduleDTO.builder().build());

            UserInfoDTO dto = userInfoService.getUserInfo(nDTO.userId());

            List<String> userList = networkService.getBookmarkUsers(userId, networkSeq);
            List<UserInfoDTO> userInfoList = userInfoService.getUserInfoList(userList);

            long currentUserCount = userInfoList.size();

            // 조회된 결과값 넣어주기
            model.addAttribute("rDTO", rDTO);
            model.addAttribute("hDTO", hDTO);
            model.addAttribute("nDTO", nDTO);
            model.addAttribute("userInfoList", userInfoList);
            model.addAttribute("currentUserCount", currentUserCount);
            model.addAttribute("dto", dto);
            model.addAttribute("userId", userId);
            model.addAttribute("roomName", roomName);

            log.info(this.getClass().getName() + ".networkEventInfo End!");

            return "network/networkEventInfo";

        } else {
            return "redirect:/user/login";
        }

    }

    /**
     * 일정 상세보기(문화시설 일정)
     */
    @GetMapping(value = "networkCultureInfo")
    public String networkCultureInfo(HttpServletRequest request, ModelMap model, HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".networkCultureInfo Start!");

        String nSeq = CmmUtil.nvl(request.getParameter("nSeq"), "");
        String userId = (String) session.getAttribute("SS_USER_ID");

        if (userId.length() > 0) {

            CultureDTO rDTO = Optional.ofNullable(cultureService.getCultureInfo(nSeq))
                    .orElseGet(() -> CultureDTO.builder().build());

            String networkSeq = CmmUtil.nvl(request.getParameter("networkSeq"), "");

            ScheduleDTO gDTO = ScheduleDTO.builder().userId(userId).networkSeq(Long.valueOf(networkSeq)).build();

            ScheduleDTO hDTO = Optional.ofNullable(networkService.getBookmarkExists(gDTO))
                    .orElseGet(() -> ScheduleDTO.builder().build());

            NetworkDTO nDTO = networkService.getNetworkInfo(networkSeq);
            UserInfoDTO dto = userInfoService.getUserInfo(nDTO.userId());

            List<String> userList = networkService.getBookmarkUsers(userId, networkSeq);
            List<UserInfoDTO> userInfoList = userInfoService.getUserInfoList(userList);

            long currentUserCount = userInfoList.size();

            String roomName = "커뮤니티 채팅방 " + networkSeq;

            if (chatService.findByRoomName(roomName)) {
                log.info("exists");
            } else {
                chatService.insertRoomNameWithSeq(roomName, userId, Long.valueOf(networkSeq));
                log.info("success");
            }

            // 조회된 결과값 넣어주기
            model.addAttribute("rDTO", rDTO);
            model.addAttribute("hDTO", hDTO);
            model.addAttribute("nDTO", nDTO);
            model.addAttribute("userInfoList", userInfoList);
            model.addAttribute("currentUserCount", currentUserCount);
            model.addAttribute("dto", dto);
            model.addAttribute("userId", userId);
            model.addAttribute("roomName", roomName);

            log.info(this.getClass().getName() + ".networkCultureInfo End!");

            return "network/networkCultureInfo";

        } else {
            return "redirect:/user/login";
        }
    }

    /**
     * 일정 삭제
     */
    @ResponseBody
    @PostMapping(value = "networkDelete")
    public MsgDTO networkDelete(HttpServletRequest request) {

        log.info(this.getClass().getName() + ".networkDelete Start!");

        String msg = "";
        MsgDTO dto = null;

        try {

            String networkSeq = CmmUtil.nvl(request.getParameter("networkSeq"));

            log.info("networkSeq : " + networkSeq);

            NetworkDTO pDTO = NetworkDTO.builder().networkSeq(Long.parseLong(networkSeq)).build();

            networkService.networkDelete(pDTO);

            msg = "삭제되었습니다.";

        } catch (Exception e) {

            msg = "실패하였습니다. : " + e.getMessage();
            log.info(e.toString());
            e.printStackTrace();

        } finally {

            dto = MsgDTO.builder().msg(msg).build();

            log.info(this.getClass().getName() + ".networkDelete End!");

        }

        return dto;
    }

    /**
     * 일정 북마크 추가
     */
    @Transactional
    @ResponseBody
    @PostMapping("/addNetwork")
    public MsgDTO addNetwork(HttpServletRequest request, HttpSession session) {

        log.info(this.getClass().getName() + ".addNetwork Start!");

        MsgDTO dto = null;
        String msg = "";
        int result = 1;

        try {
            String userId = (String) session.getAttribute("SS_USER_ID");
            String eventSeq = CmmUtil.nvl(request.getParameter("eventSeq"));

            // 현재 인원 수 조회
            NetworkDTO countDTO = networkService.countParticipants(eventSeq);
            Long currentCount = countDTO.currentCount();
            Long userCount = countDTO.userCount();

            if (currentCount < userCount) {

                ScheduleDTO pDTO = ScheduleDTO.builder()
                        .userId(userId)
                        .networkSeq(Long.valueOf(eventSeq))
                        .build();

                networkService.insertBookmark(pDTO);
                networkService.countChange(true, eventSeq);

                msg = "북마크 추가 완료!";
            } else {
                result = 2;
                msg = "북마크 추가 실패!";
            }
        } catch (Exception e) {

            msg = "실패하였습니다. : " + e.getMessage();
            log.info(e.toString());
            e.printStackTrace();

        } finally {

            dto = MsgDTO.builder().result(result).msg(msg).build();
            log.info(this.getClass().getName() + ".addNetwork End!");

        }
        return dto;
    }

    /**
     * 일정 북마크 해제
     */
    @Transactional
    @ResponseBody
    @PostMapping(value = "removeNetwork")
    public MsgDTO removeNetwork(HttpServletRequest request, HttpSession session) {

        log.info(this.getClass().getName() + ".removeNetwork Start!");

        String msg = ""; // 메시지 내용
        MsgDTO dto = null; // 결과 메시지 구조

        try {
            String userId = (String) session.getAttribute("SS_USER_ID");
            String eventSeq = CmmUtil.nvl(request.getParameter("eventSeq"));

            networkService.countChange(false, eventSeq);

            log.info("userId : " + userId);
            log.info("eventSeq : " + eventSeq);

            ScheduleDTO pDTO = ScheduleDTO.builder().userId(userId).networkSeq(Long.valueOf(eventSeq)).build();

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

        List<Long> ScheduleSeqList = rList.stream()
                .map(ScheduleDTO::networkSeq)
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
