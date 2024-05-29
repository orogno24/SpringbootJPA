package kopo.poly.controller;

import jakarta.servlet.http.HttpSession;
import kopo.poly.chat.ChatHandler;
import kopo.poly.dto.UserInfoDTO;
import kopo.poly.service.IUserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/chat")
public class ChatController {

    private final IUserInfoService userInfoService;

    /**
     * 채팅창 입장 전
     */
    @GetMapping(value = "intro")
    public String intro() {

        log.info(this.getClass().getName() + ".intro Start!");

        log.info(this.getClass().getName() + ".intro Ends!");

        return "chat/intro";
    }

    /**
     * 채팅창 접속
     */
    @PostMapping(value = "chatroom")
    public String chatroom(@RequestParam("roomName") String roomName,
                           HttpSession session,
                           Model model) throws Exception {

        log.info(this.getClass().getName() + ".chatroom Start!");

        String userId = (String) session.getAttribute("SS_USER_ID");

        UserInfoDTO dto = userInfoService.getUserInfo(userId);

        log.info("chatroom roomName: " + roomName);

        model.addAttribute("roomName", roomName);
        model.addAttribute("dto", dto);

        log.info(this.getClass().getName() + ".chatroom End!");
        return "chat/chatroom";
    }

    /**
     * 채팅방 목록
     */
    @RequestMapping(value = "roomList")
    @ResponseBody
    public Set<String> roomList() {

        log.info(this.getClass().getName() + ".roomList Start!");

        log.info(this.getClass().getName() + ".roomList Ends!");

        return ChatHandler.roomInfo.keySet();

    }
}