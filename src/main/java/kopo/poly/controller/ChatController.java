package kopo.poly.controller;

import kopo.poly.chat.ChatHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@Controller
@RequestMapping(value = "/chat")
public class ChatController {

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
                           @RequestParam("userName") String userName,
                           Model model) {

        log.info(this.getClass().getName() + ".chatroom Start!");
        log.info("chatroom roomName: " + roomName);
        log.info("chatroom userName: " + userName);

        model.addAttribute("roomName", roomName);
        model.addAttribute("userName", userName);

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