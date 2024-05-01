package kopo.poly.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.NoticeDTO;
import kopo.poly.dto.UserInfoDTO;
import kopo.poly.service.IUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
public class FileUploadController {

    private final AmazonS3 s3Client;
    private final String bucketName;
    private final IUserInfoService userInfoService;

    @Autowired
    public FileUploadController(AmazonS3 s3Client, String bucketName, IUserInfoService userInfoService) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.userInfoService = userInfoService;
    }

    // 프로필 사진 아마존 버킷에 업로드
    @PostMapping("/uploadProfileImage")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("file") MultipartFile file, HttpSession session) {

        log.info(this.getClass().getName() + ".uploadProfileImage Start!");

        String userId = (String) session.getAttribute("SS_USER_ID");

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        String fileName = "profiles/" + userId + "_" + UUID.randomUUID().toString() + "." + extension;

        try {
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), null)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            String imageUrl = s3Client.getUrl(bucketName, fileName).toString();
            UserInfoDTO pDTO = UserInfoDTO.builder().userId(userId).profilePath(imageUrl).build();
            userInfoService.profilePathProc(pDTO);

            return ResponseEntity.ok("프로필 등록에 성공했습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("프로필 등록에 실패했습니다.");
        }
    }

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

            // NoticeDTO 업데이트 로직
            NoticeDTO nDTO = NoticeDTO.builder()
                    .noticeSeq(noticeSeq)
                    .imagePath(imageUrl)
                    .build();
//            noticeService.updateNoticeImage(nDTO);

            return ResponseEntity.ok("게시글 이미지 등록에 성공했습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 이미지 등록에 실패했습니다.");
        }
    }



}
