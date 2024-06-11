package kopo.poly.controller;

import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.NetworkDTO;
import kopo.poly.service.INetworkService;
import kopo.poly.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequestMapping(value = "/network")
@RequiredArgsConstructor
public class NetworkController {

    private final INetworkService networkService;

    @GetMapping("/createNetwork")
    public String createNetwork(@RequestParam(required = false) String eventName,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) String dateTime,
                                @RequestParam(required = false) String maxParticipants,
                                @RequestParam(required = false) String selectedEventId,
                                @RequestParam(required = false) String selectedEventName,
                                Model model) throws Exception {
        log.info(this.getClass().getName() + ".createNetwork 함수 실행");

        log.info("createNetwork 호출됨: eventName={}, description={}, dateTime={}, maxParticipants={}, selectedEventId={}, selectedEventName={}",
                eventName, description, dateTime, maxParticipants, selectedEventId, selectedEventName);

        model.addAttribute("eventName", eventName);
        model.addAttribute("description", description);
        model.addAttribute("dateTime", dateTime);
        model.addAttribute("maxParticipants", maxParticipants);
        model.addAttribute("selectedEventId", selectedEventId);
        model.addAttribute("selectedEventName", selectedEventName);

        return "network/createNetwork";
    }

    @PostMapping("/createNetwork")
    public String createEvent(@RequestParam String eventName,
                              @RequestParam String description,
                              @RequestParam String dateTime,
                              @RequestParam String maxParticipants,
                              @RequestParam(required = false) String selectedEventId,
                              @RequestParam(required = false) String selectedEventName,
                              Model model, HttpSession session) {

        log.info(this.getClass().getName() + ".createNetwork 함수 실행");

        log.info("createEvent 호출됨: eventName={}, description={}, dateTime={}, maxParticipants={}, selectedEventId={}, selectedEventName={}",
                eventName, description, dateTime, maxParticipants, selectedEventId, selectedEventName);

        model.addAttribute("eventName", eventName);
        model.addAttribute("description", description);
        model.addAttribute("dateTime", dateTime);
        model.addAttribute("maxParticipants", maxParticipants);
        model.addAttribute("selectedEventId", selectedEventId);
        model.addAttribute("selectedEventName", selectedEventName);

        try {
            String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

            NetworkDTO pDTO = NetworkDTO.builder()
                    .userId(userId)
                    .name(eventName)
                    .contents(description)
                    .dateTime(LocalDateTime.parse(dateTime))
                    .maxParticipants(Integer.parseInt(maxParticipants))
                    .eventSeq(selectedEventId)
                    .cultureSeq(selectedEventName)
                    .build();

            networkService.insertNetWorkInfo(pDTO);

        } catch (Exception e) {
            log.error("등록 실패", e);
        } finally {
            log.info(this.getClass().getName() + ".createNetwork End!");
        }

        model.addAttribute("message", "Event created successfully with selected event: " + selectedEventName);
        return "network/createNetwork";
    }

}
