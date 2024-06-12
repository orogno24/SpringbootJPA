package kopo.poly.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.*;
import kopo.poly.service.IChatService;
import kopo.poly.service.IEventService;
import kopo.poly.service.INetworkService;
import kopo.poly.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping(value = "/network")
@RequiredArgsConstructor
public class NetworkController {

    private final INetworkService networkService;
    private final IEventService eventService;
    private final IChatService chatService;

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
    @GetMapping(value = "networkEventInfo")
    public String networkEventInfo(HttpServletRequest request, ModelMap model, HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".networkEventInfo Start!");

        String uniqueIdentifier = CmmUtil.nvl(request.getParameter("nSeq"), "");
        String userId = (String) session.getAttribute("SS_USER_ID");
        String eventSeq = uniqueIdentifier;

        uniqueIdentifier = "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=" + uniqueIdentifier + "&thumb=Y";

        RedisDTO redisDTO = null;
        String colNm = "EVENT_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        redisDTO = eventService.getCulturalEvents(colNm);

        ApiDTO rDTO = Optional.ofNullable(eventService.getApiInfo(redisDTO, uniqueIdentifier))
                .orElseGet(() -> ApiDTO.builder().build());

        BookmarkDTO gDTO = BookmarkDTO.builder().userId(userId).eventSeq(eventSeq).build();

        BookmarkDTO hDTO = Optional.ofNullable(eventService.getBookmarkExists(gDTO))
                .orElseGet(() -> BookmarkDTO.builder().build());

        String networkSeq = CmmUtil.nvl(request.getParameter("networkSeq"), "");
        NetworkDTO nDTO = networkService.getNetworkInfo(networkSeq);
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
        return "network/networkCultureInfo";
    }
}
