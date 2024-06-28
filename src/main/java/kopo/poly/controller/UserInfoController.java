package kopo.poly.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import kopo.poly.dto.*;
import kopo.poly.service.INoticeService;
import kopo.poly.service.IUserInfoService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping(value = "/user")
@RequiredArgsConstructor
@Controller
public class UserInfoController {

    // @RequiredArgsConstructor 를 통해 메모리에 올라간 서비스 객체를 Controller에서 사용할 수 있게 주입함
    private final IUserInfoService userInfoService;
    private final INoticeService noticeService;
    private final AmazonS3 s3Client;
    private final String bucketName;

    /**
     * 회원가입 화면으로 이동
     */
    @GetMapping(value = "userRegForm")
    public String userRegForm() {

        log.info(this.getClass().getName() + ".user/userRegForm Start!");

        log.info(this.getClass().getName() + ".user/userRegForm End!");

        return "user/userRegForm";

    }

    /**
     * 회원가입 전 아이디 중복체크하기(Ajax를 통해 입력한 아이디 정보 받음)
     */
    @ResponseBody
    @PostMapping(value = "getUserIdExists")
    public UserInfoDTO getUserExists(HttpServletRequest request) throws Exception {

        log.info(this.getClass().getName() + ".getUserIdExists Start!");

        String userId = CmmUtil.nvl(request.getParameter("userId"));

        log.info("userId : " + userId);

        // Builder 통한 값 저장
        UserInfoDTO pDTO = UserInfoDTO.builder().userId(userId).build();

        // 회원아이디를 통해 중복된 아이디인지 조회
        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserIdExists(pDTO))
                .orElseGet(() -> UserInfoDTO.builder().build());

        log.info(this.getClass().getName() + ".getUserIdExists End!");

        return rDTO;
    }

    /**
     * 회원가입 로직 처리
     */
    @ResponseBody
    @PostMapping(value = "insertUserInfo")
    public MsgDTO insertUserInfo(MultipartHttpServletRequest request, HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".insertUserInfo Start!");

        String msg; // 회원가입 결과에 대한 메시지를 전달할 변수

        String userId = CmmUtil.nvl(request.getParameter("userId"));
        String userName = CmmUtil.nvl(request.getParameter("userName"));
        String password = CmmUtil.nvl(request.getParameter("password"));
        String email = CmmUtil.nvl(request.getParameter("email"));

        log.info("userId : " + userId);
        log.info("userName : " + userName);
        log.info("password : " + password);
        log.info("email : " + email);

        MultipartFile file = request.getFile("profileImage");

        String imageUrl = "/assets/img/thumbnail.png";

        if (file != null && !file.isEmpty()) {
            try {
                String extension = FilenameUtils.getExtension(file.getOriginalFilename());
                String fileName = "profiles/" + userId + "_" + UUID.randomUUID().toString() + "." + extension;
                s3Client.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), null)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
                imageUrl = s3Client.getUrl(bucketName, fileName).toString();
            } catch (Exception e) {
                log.error("파일 업로드 실패: " + e.getMessage());
                return MsgDTO.builder().result(0).msg("파일 업로드 실패").build();
            }
        }

        // 웹 (회원정보 입력화면)에서 받는 정보를 저장할 변수를 메모리에 올리기
        UserInfoDTO pDTO = UserInfoDTO.builder()
                .userId(userId)
                .userName(userName)
                .password(EncryptUtil.encHashSHA256(password))
                .email(EncryptUtil.encAES128CBC(email))
                .regId(userId)
                .chgId(userId)
                .profilePath(imageUrl)
                .build();

        // 회원가입 서비스 호출하여 결과 받기
        int res = userInfoService.insertUserInfo(pDTO);

        log.info("회원가입 결과(res) : " + res);

        if (res == 1) {
            msg = "회원가입되었습니다.";
            session.setAttribute("SS_USER_ID", userId);
        } else if (res == 2) {
            msg = "이미 가입된 아이디입니다.";
        } else if (res == 3) {
            msg = "이미 가입된 이메일입니다.";
        } else {
            msg = "오류로 인해 회원가입이 실패하였습니다.";
        }

        // 결과 메시지 전달하기
        MsgDTO dto = MsgDTO.builder().result(res).msg(msg).build();

        log.info(this.getClass().getName() + ".insertUserInfo End!");

        return dto;
    }

    /**
     * 로그인을 위한 입력 화면으로 이동
     */
    @GetMapping(value = "login")
    public String login() {
        log.info(this.getClass().getName() + ".user/login Start!");

        log.info(this.getClass().getName() + ".user/login End!");

        return "user/login";
    }

    /**
     * 키워드 추가 페이지
     */
    @GetMapping(value = "addKeyword")
    public String addKeyword() {
        log.info(this.getClass().getName() + ".user/addKeyword Start!");

        log.info(this.getClass().getName() + ".user/addKeyword End!");

        return "user/addKeyword";
    }

    /**
     * 키워드 수정 페이지
     */
    @GetMapping(value = "keywordChange")
    public String keywordChange() {
        log.info(this.getClass().getName() + ".user/keywordChange Start!");

        log.info(this.getClass().getName() + ".user/keywordChange End!");

        return "user/keywordChange";
    }

    /**
     * 키워드 추가 로직
     */
    @PostMapping("/addKeywordProc")
    public ResponseEntity<?> addKeywords(@RequestBody List<String> keywords, HttpSession session) {

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        if (userId == null) {
            return ResponseEntity.badRequest().body("사용자 인증이 필요합니다.");
        }

        try {
            // 키워드 저장 서비스 호출
            userInfoService.saveKeywordsForUser(userId, keywords);
            session.setAttribute("SS_USER_ID", "");
            session.removeAttribute("SS_USER_ID");
            return ResponseEntity.ok("키워드가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            // 로깅 추가
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("키워드 저장 중 오류가 발생했습니다.");
        }
    }

    /**
     * 키워드 수정 로직
     */
    @PostMapping("/updateKeywords")
    public ResponseEntity<?> updateKeywords(@RequestBody List<String> keywords, HttpSession session) {

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        if (userId == null) {
            return ResponseEntity.badRequest().body("사용자 인증이 필요합니다.");
        }
        try {
            userInfoService.updateKeywords(userId, keywords);
            return ResponseEntity.ok("키워드가 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            log.error("키워드 업데이트 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("키워드 업데이트 중 오류가 발생했습니다.");
        }
    }

    /**
     * 키워드 조회 로직
     */
    @ResponseBody
    @GetMapping(value = "getKeywords")
    public List<String> getKeywords(HttpServletRequest request, HttpSession session)
            throws Exception {

        log.info(this.getClass().getName() + ".getKeywords start!");

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        List<UserInterestsDTO> rList = Optional.ofNullable(userInfoService.getKeywordList(userId))
                .orElseGet(ArrayList::new);

        // keywords 리스트에서 keyword 항목만 추출하여 List<String> 타입으로 변환
        List<String> interestKeywords = rList.stream()
                .map(UserInterestsDTO::keyword) // UserInterestsDTO 객체에서 keyword 항목을 추출
                .collect(Collectors.toList());

        log.info(this.getClass().getName() + ".getKeywords end!");

        return interestKeywords;
    }

    /**
     * 로그인 처리 및 결과 알려주는 화면으로 이동
     */
    @ResponseBody
    @PostMapping(value = "loginProc")
    public MsgDTO loginProc(HttpServletRequest request, HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".loginProc Start!");

        String msg; // 로그인 결과에 대한 메시지를 전달할 변수

        String userId = CmmUtil.nvl(request.getParameter("userId"));
        String password = CmmUtil.nvl(request.getParameter("password"));

        log.info("userId : " + userId);
        log.info("password : " + password);

        // 웹(회원정보 입력화면)에서 받는 정보를 저장할 변수를 메모리에 올리기
        UserInfoDTO pDTO = UserInfoDTO.builder()
                .userId(userId)
                .password(EncryptUtil.encHashSHA256(password)).build();

        // 로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기 위한 usreInfoService 호춣하기
        int res = userInfoService.getUserLogin(pDTO);
        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserIdExists(pDTO))
                .orElseGet(() -> UserInfoDTO.builder().build());

        log.info("res : " + res);

        /*
         * 스프링에서 세션을 사용하기 위해서는 함수명의 파라미터에 HttpSession session 존재해야 한다.
         * 세션은 톰켓의 메모리에 저장되기 때문에 url마다 전달하는게 필요하지 않고,
         * 그냥 메모리에서 부르면 되기 때문에 jsp, controller에서 쉽게 불러서 쓸 수 있다.
         */

        if (res == 1) { // 로그인 성공

            /*
             * 세션에 회원아이디 저장하기, 추후 로그인여부를 체크하기 위해 세션에 값이 존재하는지 체크한다.
             * 일반적으로 세션에 저장되는 키는 대문자로 입력하며, 앞에 SS를 붙인다.
             *
             * Session 단어에서 SS를 가져온 것이다.
             */
            msg = "로그인이 성공했습니다.";
            session.setAttribute("SS_USER_ID", userId);
            session.setAttribute("SS_USER_NAME", rDTO.userName());

            log.info("SS_USER_ID : " + rDTO.userId());
            log.info("SS_USER_NAME : " + rDTO.userName());
        } else {
            msg = "아이디와 비밀번호가 올바르지 않습니다.";

        }

        // 결과 메시지 전달하기
        MsgDTO dto = MsgDTO.builder().result(res).msg(msg).build();
        log.info(this.getClass().getName() + "loginProc End!");

        return dto;
    }

    /**
     * 로그인 성공 페이지 이동
     */
    @GetMapping(value = "loginSuccess")
    public String loginSuccess() {
        log.info(this.getClass().getName() + ".user/loginSuccess Start!");

        log.info(this.getClass().getName() + ".user/loginSuccess End!");

        return "user/loginSuccess";
    }

    /**
     * 회원탈퇴 페이지
     */
    @GetMapping(value = "deleteUser")
    public String deleteUser() {

        log.info(this.getClass().getName() + ".user/deleteUser Start!");

        log.info(this.getClass().getName() + ".user/deleteUser End!");

        return "user/deleteUser";

    }

    /**
     * 회원탈퇴 과정
     */
    @ResponseBody
    @GetMapping(value = "deleteUserProc")
    public ResponseEntity<?> deleteUserProc(HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".user/deleteUserProc Start!");

        String userId = (String) session.getAttribute("SS_USER_ID");
        log.info("userId : " + userId);

        UserInfoDTO userInfo = userInfoService.getUserInfo(userId);

        if (userInfo != null && userInfo.profilePath() != null && !userInfo.profilePath().isEmpty()) {
            try {
                // 프로필 이미지 경로에서 파일 이름 추출
                String fileName = userInfo.profilePath().substring(userInfo.profilePath().lastIndexOf("/") + 1);

                // S3에서 파일 삭제
                s3Client.deleteObject(bucketName, "profiles/" + fileName);
                log.info("S3 버킷에서 파일 삭제 성공: " + fileName);
            } catch (Exception e) {
                log.error("S3 버킷에서 파일 삭제 실패: " + e.getMessage());
            }
        }

        UserInfoDTO pDTO = UserInfoDTO.builder().userId(userId).build();

        // 회원가입 서비스 호출하여 결과 받기
        userInfoService.deleteUserProc(pDTO);

        log.info(this.getClass().getName() + ".user/deleteUserProc End!");

        return ResponseEntity.ok().body("회원탈퇴 성공!");
    }

    /**
     * 유저 프로필
     */
    @GetMapping(value = "userProfile/{userId}")  // URL 경로 변경
    public String userProfile(@PathVariable("userId") String userId, HttpSession session, ModelMap model) throws Exception {
        log.info(this.getClass().getName() + ".user/userProfile Start!");

        // 세션에서 로그인한 사용자의 ID를 가져옵니다.
        String ssUserId = (String) session.getAttribute("SS_USER_ID");

        // URL에서 받은 userId를 사용하여 사용자 정보를 가져옵니다.
        UserInfoDTO dto = userInfoService.getUserInfo(userId);

        // 팔로우 상태를 확인하기 위한 DTO 생성
        UserFollowDTO pDTO = UserFollowDTO.builder().followingId(userId).followerId(ssUserId).build();

        long countByFollowerId = userInfoService.countByFollowerId(userId);
        long countByFollowingId = userInfoService.countByFollowingId(userId);
        long countByUserId = noticeService.countByUserId(userId);

        // 팔로우 상태 확인
        boolean followStatus = userInfoService.getFollowInfo(pDTO);
        String existsYn = followStatus ? "Y" : "N";

        log.info("countByFollowerId : " + countByFollowerId);
        log.info("countByFollowingId : " + countByFollowingId);
        log.info("countByUserId : " + countByUserId);

        // 모델에 사용자 정보와 팔로우 상태를 추가
        model.addAttribute("dto", dto);
        model.addAttribute("existsYn", existsYn);
        model.addAttribute("countByFollowerId", countByFollowerId);
        model.addAttribute("countByFollowingId", countByFollowingId);
        model.addAttribute("countByUserId", countByUserId);

        log.info(this.getClass().getName() + ".user/userProfile End!");

        return "user/userProfile";
    }


    /**
     * 유저 프로필 설정
     */
    @GetMapping(value = "userProfileEdit")
    public String userProfileEdit(HttpSession session, ModelMap model) throws Exception {
        log.info(this.getClass().getName() + ".user/userProfileEdit Start!");

        String userId = (String) session.getAttribute("SS_USER_ID");

        UserInfoDTO pDTO = userInfoService.getUserInfo(userId);

        String profilePath = pDTO.profilePath() != null && !pDTO.profilePath().isEmpty() ? pDTO.profilePath() : "/assets/img/profile.png";

        log.info("profilePath : " + profilePath);
        log.info("userName : " + pDTO.userName());

        model.addAttribute("profilePath", profilePath);
        model.addAttribute("pDTO", pDTO);

        log.info(this.getClass().getName() + ".user/userProfileEdit End!");

        return "user/userProfileEdit";
    }

    /**
     * 팔로우 추가
     */
    @ResponseBody
    @PostMapping(value = "addFollow")
    public MsgDTO addFollow(HttpServletRequest request, HttpSession session, ModelMap model) throws Exception {

        log.info(this.getClass().getName() + ".user/addFollow Start!");

        MsgDTO dto = null;
        String msg = "";

        try {

            String followUserId = CmmUtil.nvl(request.getParameter("followUserId"));
            String userId = (String) session.getAttribute("SS_USER_ID");

            log.info("followUserId : " + followUserId);
            log.info("userId : " + userId);

            UserFollowDTO pDTO = UserFollowDTO.builder().followingId(followUserId).followerId(userId).build();

            userInfoService.addFollower(pDTO);

            msg = "팔로우 추가 완료!";

        } catch (Exception e) {

            msg = "실패하였습니다. : " + e.getMessage();
            log.info(e.toString());
            e.printStackTrace();

        } finally {

            dto = MsgDTO.builder().msg(msg).build();

            log.info(this.getClass().getName() + ".user/addFollow End!");

        }

        return dto;

    }

    /**
     * 팔로우 제거
     */
    @ResponseBody
    @PostMapping(value = "removeFollow")
    public MsgDTO removeFollow(HttpServletRequest request, HttpSession session) {

        log.info(this.getClass().getName() + ".removeFollow Start!");

        String msg = ""; // 메시지 내용
        MsgDTO dto = null; // 결과 메시지 구조

        try {
            String followUserId = CmmUtil.nvl(request.getParameter("followUserId"));
            String userId = (String) session.getAttribute("SS_USER_ID");

            log.info("followUserId : " + followUserId);
            log.info("userId : " + userId);

            UserFollowDTO pDTO = UserFollowDTO.builder().followingId(followUserId).followerId(userId).build();

            userInfoService.removeFollower(pDTO);

            msg = "삭제되었습니다.";

        } catch (Exception e) {
            msg = "실패하였습니다. : " + e.getMessage();
            log.info(e.toString());
            e.printStackTrace();

        } finally {
            // 결과 메시지 전달하기
            dto = MsgDTO.builder().msg(msg).build();

            log.info(this.getClass().getName() + ".removeFollow End!");

        }

        return dto;
    }

    /**
     * 아이디 찾기 페이지
     */
    @GetMapping(value = "searchUserId")
    public String searchUserId() {
        log.info(this.getClass().getName() + ".user/searchUserId Start!");

        log.info(this.getClass().getName() + ".user/searchUserId End!");

        return "user/searchUserId";
    }

    /**
     * 아이디 찾기
     */
    @ResponseBody
    @PostMapping(value = "searchUserIdProc")
    public MsgDTO searchUserIdProc(HttpServletRequest request, ModelMap model) throws Exception {

        log.info(this.getClass().getName() + ".user/searchUserIdProc Start!");

        String msg;

        String userName = CmmUtil.nvl(request.getParameter("userName"));
        String email = CmmUtil.nvl(request.getParameter("email"));

        log.info("userName : " + userName);
        log.info("email : " + email);

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .userName(userName)
                .email(EncryptUtil.encAES128CBC(email))
                .build();

        String res = userInfoService.searchUserIdProc(pDTO);

        if (!Objects.equals(res, "")) {
            msg = userName + " 회원님의 아이디는 " + res + "입니다.";
        } else {
            msg = "회원정보가 일치하지 않습니다.";
        }

        MsgDTO dto = MsgDTO.builder().msg(msg).build();

//        model.addAttribute("rDTO", rDTO);

        log.info(this.getClass().getName() + ".user/searchUserIdProc End!");

        return dto;
    }

    /**
     * 이메일 중복찾기
     */
    @ResponseBody
    @PostMapping(value = "getEmailExists")
    public UserInfoDTO getEmailExists(HttpServletRequest request) throws Exception {

        log.info(this.getClass().getName() + ".getEmailExists Start!");

        String email = CmmUtil.nvl(request.getParameter("email"));

        log.info("email : " + email);
        UserInfoDTO pDTO = UserInfoDTO.builder()
                .email(EncryptUtil.encAES128CBC(email))
                .build();

        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getEmailExists(pDTO))
                .orElseGet(() -> UserInfoDTO.builder().build());

        log.info(this.getClass().getName() + ".getEmailExists End!");

        return rDTO;
    }

    /**
     * 비밀번호 찾기 페이지
     */
    @GetMapping(value = "searchPassword")
    public String searchPassword(HttpSession session) {
        log.info(this.getClass().getName() + ".user/searchPassword Start!");

        session.setAttribute("USER_ID", "");
        session.removeAttribute("USER_ID");

        log.info(this.getClass().getName() + ".user/searchPassword End!");

        return "user/searchPassword";
    }

    /**
     * 비밀번호 찾기
     */
    @PostMapping(value = "searchPasswordProc")
    public String searchPasswordProc(HttpServletRequest request, ModelMap model, HttpSession session) throws Exception {
        log.info(this.getClass().getName() + ".user/searchPasswordProc start!");

        String userId = CmmUtil.nvl(request.getParameter("userId"));
        String userName = CmmUtil.nvl(request.getParameter("userName"));
        String email = CmmUtil.nvl(request.getParameter("email"));

        log.info("userId : " + userId);
        log.info("userName : " + userName);
        log.info("email : " + email);

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .userId(userId)
                .userName(userName)
                .email(EncryptUtil.encAES128CBC(email))
                .build();

        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.searchPasswordProc(pDTO))
                .orElseGet(() -> UserInfoDTO.builder().build());

        model.addAttribute("rDTO", rDTO);

        session.setAttribute("USER_ID", userId);

        log.info(this.getClass().getName() + ".user/searchPasswordProc end!");

        return "user/newPassword";
    }

    /**
     * 닉네임 변경 페이지
     */
    @GetMapping(value = "changeUserName")
    public String changeUserName() throws Exception {
        log.info(this.getClass().getName() + ".changeUserName Start!");

        return "user/changeUserName";
    }

    /**
     * 닉네임 변경하기
     */
    @ResponseBody
    @PostMapping(value = "newUserNameProc")
    public MsgDTO newUserNameProc(HttpServletRequest request, HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".newUserNameProc Start!");

        MsgDTO dto = null;
        String msg = "";

        try {
            String userId = (String) session.getAttribute("SS_USER_ID");
            String userName = request.getParameter("userName");
            log.info("userId : " + userId);
            log.info("userName : " + userName);
            UserInfoDTO pDTO = UserInfoDTO.builder()
                    .userId(userId)
                    .userName(userName)
                    .build();

            userInfoService.newUserNameProc(pDTO);

            session.setAttribute("SS_USER_NAME", userName);

            msg = "닉네임이 재설정되었습니다.";

        } catch (Exception e) {
            msg = "시스템 문제로 닉네임 변경이 실패하였습니다." + e.getMessage();
            log.info(e.toString());
            e.printStackTrace();
        } finally {
            // 결과 메시지 전달하기
            dto = MsgDTO.builder().msg(msg).build();

        }

        log.info(this.getClass().getName() + ".newUserNameProc End!");

        return dto;
    }

    /**
     * 비밀번호 변경
     */
    @ResponseBody
    @PostMapping(value = "newPasswordProc")
    public MsgDTO newPasswordProc(HttpServletRequest request, HttpSession session) throws Exception {
        log.info(this.getClass().getName() + ".user/newPasswordProc Start!");
        String msg = "";
        MsgDTO dto = null;

        String userId = CmmUtil.nvl((String) session.getAttribute("USER_ID"));

        if (userId.length() > 0) {
            try {
                String password = CmmUtil.nvl(request.getParameter("password"));

                log.info("password : " + password);

                UserInfoDTO pDTO = UserInfoDTO.builder()
                        .userId(userId)
                        .password(EncryptUtil.encHashSHA256(password))
                        .build();

                userInfoService.newPasswordProc(pDTO);

                session.setAttribute("USER_ID", "");
                session.removeAttribute("USER_ID");

                msg = "비밀번호가 재설정되었습니다.";
            } catch (Exception e) {
                msg = "실패하였습니다. : " + e.getMessage();
                log.info(e.toString());
                e.printStackTrace();

            } finally {
                // 결과 메시지 전달하기
                dto = MsgDTO.builder().msg(msg).build();

                log.info(this.getClass().getName() + ".newPasswordProc End!");
            }
        }

        return dto;
    }

    /**
     * 특정 유저의 팔로우 리스트
     */
    @Transactional
    @GetMapping(value = "followList/{userId}")
    public String followList(@PathVariable("userId") String userId, ModelMap model)
            throws Exception {

        log.info(this.getClass().getName() + ".followList Start!");

        UserInfoDTO rDTO = userInfoService.getUserInfo(userId);

        List<UserFollowDTO> rList = Optional.ofNullable(userInfoService.getFollowList(userId))
                .orElseGet(ArrayList::new);

        log.info("rList : " + rList);

        model.addAttribute("profileUserName", rDTO.userName());
        model.addAttribute("rList", rList);

        log.info(this.getClass().getName() + ".followList End!");

        return "user/followList";
    }

    /**
     * 특정 유저의 팔로잉 리스트
     */
    @Transactional
    @GetMapping(value = "followingList/{userId}")
    public String followingList(@PathVariable("userId") String userId, ModelMap model)
            throws Exception {

        log.info(this.getClass().getName() + ".followingList Start!");

        UserInfoDTO rDTO = userInfoService.getUserInfo(userId);

        List<UserFollowDTO> rList = Optional.ofNullable(userInfoService.getFollowingList(userId))
                .orElseGet(ArrayList::new);

        log.info("rList : " + rList);

        model.addAttribute("profileUserName", rDTO.userName());
        model.addAttribute("rList", rList);

        log.info(this.getClass().getName() + ".followingList End!");

        return "user/followingList";
    }

}
