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
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    @GetMapping("/createNetwork")
    public String createNetwork(@RequestParam(required = false) String eventName,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) String dateTime,
                                @RequestParam(required = false) String maxParticipants,
                                @RequestParam(required = false) String selectedEventId,
                                @RequestParam(required = false) String selectedEventName,
                                @RequestParam(required = false) String ImagePath,
                                Model model) throws Exception {
        log.info(this.getClass().getName() + ".createNetwork 함수 실행");

        model.addAttribute("eventName", eventName);
        model.addAttribute("description", description);
        model.addAttribute("dateTime", dateTime);
        model.addAttribute("maxParticipants", maxParticipants);
        model.addAttribute("selectedEventId", selectedEventId);
        model.addAttribute("selectedEventName", selectedEventName);
        model.addAttribute("ImagePath", ImagePath);

        return "network/createNetwork";
    }

    @PostMapping("/createNetwork")
    public String createNetwork(@RequestParam String eventName,
                              @RequestParam String description,
                              @RequestParam String dateTime,
                              @RequestParam String maxParticipants,
                              @RequestParam(required = false) String selectedEventId,
                              @RequestParam(required = false) String selectedEventName,
                              @RequestParam(required = false) String ImagePath,
                              Model model, HttpSession session) {

        log.info(this.getClass().getName() + ".createNetwork 함수 실행");

        model.addAttribute("eventName", eventName);
        model.addAttribute("description", description);
        model.addAttribute("dateTime", dateTime);
        model.addAttribute("maxParticipants", maxParticipants);
        model.addAttribute("selectedEventId", selectedEventId);
        model.addAttribute("selectedEventName", selectedEventName);
        model.addAttribute("ImagePath", ImagePath);

        return "network/createNetwork";
    }

    @ResponseBody
    @Transactional
    @PostMapping("/insertNetwork")
    public MsgDTO insertNetwork(@RequestParam String eventName,
                                @RequestParam String description,
                                @RequestParam String dateTime,
                                @RequestParam String maxParticipants,
                                @RequestParam(required = false) String selectedEventId,
                                @RequestParam(required = false) String selectedEventName,
                                @RequestParam(required = false) String ImagePath,
                                HttpSession session) {

        log.info(this.getClass().getName() + ".insertNetwork 함수 실행");
        String msg = "";
        MsgDTO dto = null;

        try {
            String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
            String selectedTime = CmmUtil.nvl(dateTime);

            NetworkDTO pDTO = NetworkDTO.builder()
                    .userId(userId)
                    .name(eventName)
                    .contents(description)
                    .dateTime(selectedTime)
                    .maxParticipants(Long.valueOf(maxParticipants))
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

        // 조회된 결과값 넣어주기
        model.addAttribute("rDTO", rDTO);
        model.addAttribute("hDTO", hDTO);
        model.addAttribute("nDTO", nDTO);
        model.addAttribute("userId", userId);
//        model.addAttribute("roomName", networkSeq);

        log.info(this.getClass().getName() + ".networkEventInfo End!");

        return "network/networkEventInfo";
    }

    @GetMapping(value = "networkCultureInfo")
    public String networkCultureInfo(HttpServletRequest request, ModelMap model, HttpSession session) throws Exception {
        return "network/networkCultureInfo";
    }
}
