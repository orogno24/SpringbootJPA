package kopo.poly.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import kopo.poly.controller.response.CommonResponse;
import kopo.poly.dto.*;
import kopo.poly.service.INoticeService;
import kopo.poly.service.IUserInfoService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    // Spring Security에서 제공하는 비밀번호 암호화 객체(해시 함수)
    private final PasswordEncoder bCryptPasswordEncoder;

    @PostMapping(value = "userInfo")
    public ResponseEntity<CommonResponse> userInfo(HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".userInfo Start!");

        // Session 저장된 로그인한 회원아이디 가져오기
        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        UserInfoDTO pDTO = UserInfoDTO.builder().userId(userId).build();

        // 회원정보 조회하기
        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserInfo(pDTO.userId()))
                .orElseGet(() -> UserInfoDTO.builder().build());

        log.info(this.getClass().getName() + ".userInfo End!");

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO));

    }

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
    @GetMapping(value = "getUserIdExists")
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
                .password(bCryptPasswordEncoder.encode(password))
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
    @DeleteMapping(value = "deleteUserProc")
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

        // 키워드 리스트 조회
        List<UserInterestsDTO> rList = Optional.ofNullable(userInfoService.getKeywordList(userId))
                .orElseGet(ArrayList::new);

        // keywords 리스트에서 keyword 항목만 추출하여 List<String> 타입으로 변환
        List<String> interestKeywords = rList.stream()
                .map(UserInterestsDTO::keyword) // UserInterestsDTO 객체에서 keyword 항목을 추출
                .collect(Collectors.toList());

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
        model.addAttribute("interestKeywords", interestKeywords);
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
