package kopo.poly.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kopo.poly.controller.response.CommonResponse;
import kopo.poly.dto.MsgDTO;
import kopo.poly.dto.UserInfoDTO;
import kopo.poly.service.IUserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@Slf4j
@RequestMapping(value = "/reg")
@RequiredArgsConstructor
@RestController
public class UserRegController {

    private final IUserInfoService userInfoService;
    private final AmazonS3 s3Client;
    private final String bucketName;

    // Spring Security에서 제공하는 비밀번호 암호화 객체(해시 함수)
    private final PasswordEncoder bCryptPasswordEncoder;

    @PostMapping(value = "getUserIdExists")
    public ResponseEntity<CommonResponse> getUserIdExists(@RequestBody UserInfoDTO pDTO) throws Exception {
        log.info(this.getClass().getName() + ".getUserIdExists Start!");

        UserInfoDTO rDTO = userInfoService.getUserIdExists(pDTO);

        log.info(this.getClass().getName() + ".getUserIdExists End!");

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO));
    }

    @PostMapping(value = "insertUserInfo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse> insertUserInfo(@Valid @ModelAttribute UserInfoDTO pDTO,
                                                         BindingResult bindingResult, HttpSession session) {

        log.info(this.getClass().getName() + ".insertUserInfo Start!");

        if (bindingResult.hasErrors()) { // Spring Validation 맞춰 잘 바인딩되었는지 체크
            return CommonResponse.getErrors(bindingResult); // 유효성 검증 결과에 따른 에러 메시지 전달
        }

        int res = 0; // 회원가입 결과
        String msg = ""; //회원가입 결과에 대한 메시지를 전달할 변수
        MsgDTO dto; // 결과 메시지 구조
        String imageUrl = "/assets/img/thumbnail.png";

        log.info("pDTO : " + pDTO);

        if (pDTO.profileImage() != null && !pDTO.profileImage().isEmpty()) {
            try {
                String extension = FilenameUtils.getExtension(pDTO.profileImage().getOriginalFilename());
                String fileName = "profiles/" + pDTO.userId() + "_" + UUID.randomUUID().toString() + "." + extension;
                s3Client.putObject(new PutObjectRequest(bucketName, fileName, pDTO.profileImage().getInputStream(), null)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
                imageUrl = s3Client.getUrl(bucketName, fileName).toString();
            } catch (Exception e) {
                log.error("파일 업로드 실패: " + e.getMessage());
            }
        } else {
            log.info("사진 업로드 안됨");
        }

        try {
            log.info("imageUrl : " + imageUrl);

            // 웹으로 입력받은 정보와 비밀번호 생성하기
            UserInfoDTO nDTO = UserInfoDTO.createUser(
                    pDTO, bCryptPasswordEncoder.encode(pDTO.password()), imageUrl);

            res = userInfoService.insertUserInfo(nDTO);

            log.info("회원가입 결과(res) : " + res);

            if (res == 1) {
                msg = "회원가입되었습니다.";
                session.setAttribute("SS_USER_ID", pDTO.userId());
            } else if (res == 2) {
                msg = "이미 가입된 아이디입니다.";
            } else {
                msg = "오류로 인해 회원가입이 실패하였습니다.";
            }

        } catch (Exception e) {
            //저장이 실패되면 사용자에게 보여줄 메시지
            msg = "실패하였습니다. : " + e;
            res = 2;
            log.info(e.toString());

        } finally {
            dto = MsgDTO.builder().result(res).msg(msg).build();

            log.info(this.getClass().getName() + ".insertUserInfo End!");
        }

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));
    }

}