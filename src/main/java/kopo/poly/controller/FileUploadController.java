package kopo.poly.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.UserInfoDTO;
import kopo.poly.service.IUserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileUploadController {

    private final AmazonS3 s3Client;
    private final String bucketName;
    private final IUserInfoService userInfoService;

    // 프로필 사진 아마존 버킷에 업로드
    @PostMapping("/profileChange")
    public ResponseEntity<?> profileChange(HttpServletRequest request,
                                           @RequestParam(value = "file", required = false) MultipartFile file,
                                           @RequestParam(value = "userName", required = false) String userName,
                                           HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".profileChange Start!");

        String userId = (String) session.getAttribute("SS_USER_ID");

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .userId(userId)
                .userName(userName)
                .build();

        userInfoService.newUserNameProc(pDTO);

        UserInfoDTO existingUserDTO = userInfoService.getUserInfo(userId);

        log.info("userName: " + userName);

        if (file != null && !file.isEmpty()) {

            // 파일이 제공되었고, 기존 이미지 URL이 존재하면 S3에서 기존 이미지 삭제
            if (existingUserDTO != null && existingUserDTO.profilePath() != null) {
                URL oldProfileUrl = new URL(existingUserDTO.profilePath());
                String olds3ProfilePath = oldProfileUrl.getPath().substring(1); // URL에서 객체 키 추출 (앞의 '/' 제거)
                log.info("olds3ProfilePath: " + olds3ProfilePath);
                s3Client.deleteObject(bucketName, olds3ProfilePath);
            }

            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String fileName = "profiles/" + userId + "_" + UUID.randomUUID().toString() + "." + extension;

            try {
                // S3에 프로필 이미지 업로드
                s3Client.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), null)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
                String imageUrl = s3Client.getUrl(bucketName, fileName).toString();

                // 이미지 경로를 데이터베이스에 저장
                UserInfoDTO iDTO = UserInfoDTO.builder().userId(userId).profilePath(imageUrl).build();
                userInfoService.profilePathProc(iDTO);

                return ResponseEntity.ok("프로필 업데이트 성공!");
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("프로필 등록에 실패했습니다.");
            }
        }

        return ResponseEntity.ok("프로필 업데이트 성공!");
    }

    // 게시글에 이미지 업로드
    @PostMapping("/uploadNoticeImage")
    public ResponseEntity<?> uploadNoticeImage(@RequestParam("file") MultipartFile file, @RequestParam("noticeSeq") Long noticeSeq, HttpSession session) {
        log.info(this.getClass().getName() + ".uploadNoticeImage Start!");

        try {
            String userId = (String) session.getAttribute("SS_USER_ID");
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String fileName = "notices/" + userId + "_" + noticeSeq + "_" + UUID.randomUUID().toString() + "." + extension;

            s3Client.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), null)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            String imageUrl = s3Client.getUrl(bucketName, fileName).toString();

            return ResponseEntity.ok("게시글 이미지 등록에 성공했습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 이미지 등록에 실패했습니다.");
        }
    }

}
