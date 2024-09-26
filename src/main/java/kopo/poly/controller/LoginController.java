package kopo.poly.controller;

import jakarta.servlet.http.HttpSession;
import kopo.poly.auth.AuthInfo;
import kopo.poly.controller.response.CommonResponse;
import kopo.poly.dto.MsgDTO;
import kopo.poly.dto.UserInfoDTO;
import kopo.poly.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;


@Slf4j
@RequestMapping(value = "/login")
@RequiredArgsConstructor
@RestController
public class LoginController {

    @PostMapping(value = "loginSuccess")
    public ResponseEntity<CommonResponse> loginSuccess(@AuthenticationPrincipal AuthInfo authInfo, HttpSession session) {

        log.info(this.getClass().getName() + ".loginSuccess Start!");

        // Spring Security에 저장된 정보 가져오기
        UserInfoDTO rDTO = Optional.ofNullable(authInfo.getUserInfoDTO()).orElseGet(() -> UserInfoDTO.builder().build());

        String userId = CmmUtil.nvl(rDTO.userId());
        String userName = CmmUtil.nvl(rDTO.userName());

        log.info("userId : " + userId);
        log.info("userName : " + userName);

        session.setAttribute("SS_USER_ID", userId);
        session.setAttribute("SS_USER_NAME", userName);

        // 결과 메시지 전달하기
        MsgDTO dto = MsgDTO.builder().result(1).msg(userName + "님 로그인이 성공하였습니다.").build();

        log.info(this.getClass().getName() + ".loginSuccess End!");

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));

    }

    @PostMapping(value = "loginFail")
    public ResponseEntity<CommonResponse> loginFail() {

        log.info(this.getClass().getName() + ".loginFail Start!");

        // 결과 메시지 전달하기
        MsgDTO dto = MsgDTO.builder().result(2).msg("로그인이 실패하였습니다.").build();

        log.info(this.getClass().getName() + ".loginFail End!");

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));

    }

    /**
     * 로그인 정보 가져오기
     */
    @PostMapping(value = "loginInfo")
    public ResponseEntity<CommonResponse> loginInfo(HttpSession session) {

        log.info(this.getClass().getName() + ".loginInfo Start!");

        // Session 저장된 로그인한 회원 정보 가져오기
        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
        String userName = CmmUtil.nvl((String) session.getAttribute("SS_USER_NAME"));

        // 세션 값 전달할 데이터 구조 만들기
        UserInfoDTO dto = UserInfoDTO.builder().userId(userId).userName(userName).build();

        log.info(this.getClass().getName() + ".loginInfo End!");

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));

    }

}