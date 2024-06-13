package kopo.poly.controller;

import jakarta.servlet.http.HttpSession;
import kopo.poly.chat.ChatHandler;
import kopo.poly.dto.UserInfoDTO;
import kopo.poly.service.IChatService;
import kopo.poly.service.IUserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/chat")
public class ChatController {

    private final IUserInfoService userInfoService;
    private final IChatService chatService;

    /**
     * 채팅창 목록 조회 페이지
     */
    @GetMapping(value = "intro")
    public String intro() {

        log.info(this.getClass().getName() + ".intro Start!");

        log.info(this.getClass().getName() + ".intro Ends!");

        return "chat/intro";
    }

    /**
     * 채팅방 생성
     */
    @PostMapping("/chatroom")
    @ResponseBody
    public ResponseEntity<?> createChatRoom(@RequestParam("roomName") String roomName, @RequestParam("userId") String userId, HttpSession session, Model model) {
        try {

            log.info(this.getClass().getName() + ".createChatRoom Start!");

            String sessionUserId = (String) session.getAttribute("SS_USER_ID");

            UserInfoDTO dto = userInfoService.getUserInfo(sessionUserId);

            log.info("chatroom roomName: " + roomName);

            model.addAttribute("roomName", roomName);
            model.addAttribute("dto", dto);

            log.info(this.getClass().getName() + ".createChatRoom End!");

            if (chatService.findByRoomName(roomName)) {
                log.info("exists");
                return ResponseEntity.ok().body(Collections.singletonMap("status", "exists"));
            } else {
                chatService.insertRoomName(roomName, userId);
                log.info("success");
                return ResponseEntity.ok().body(Collections.singletonMap("status", "success"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("status", "error"));
        }

    }

    /**
     * 개설된 채팅방 접속
     */
    @GetMapping("/chatroom/{roomName}")
    public String chatroom(@PathVariable("roomName") String roomName,
                           HttpSession session,
                           Model model) throws Exception {

        log.info(this.getClass().getName() + ".chatroom Start!");

        String userId = (String) session.getAttribute("SS_USER_ID");

        if (userId.length() > 0) {

            UserInfoDTO dto = userInfoService.getUserInfo(userId);
            String roomOwner = chatService.getRoomOwner(roomName);

            log.info("chatroom roomName: " + roomName);
            log.info("chatroom roomOwner: " + roomOwner);

            model.addAttribute("roomName", roomName);
            model.addAttribute("dto", dto);
            model.addAttribute("roomOwner", roomOwner);

        } else {

            return "redirect:/user/login";

        }

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

    /**
     * 채팅방 삭제
     */
    @GetMapping(value = "deleteRoom/{roomName}")
    @ResponseBody
    public String deleteRoom(@PathVariable("roomName") String roomName) throws Exception {
        log.info(this.getClass().getName() + ".deleteRoom Start!");

        if (ChatHandler.roomInfo.containsKey(roomName)) {
            chatService.deleteRoomName(roomName);
            ChatHandler.roomInfo.remove(roomName);
            log.info("Room " + roomName + " deleted.");
        } else {
            log.info("Room " + roomName + " not found.");
        }

        log.info(this.getClass().getName() + ".deleteRoom Ends!");
        return "Success";
    }
}