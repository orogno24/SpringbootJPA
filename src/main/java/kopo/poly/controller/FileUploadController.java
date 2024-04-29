package kopo.poly.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.servlet.http.HttpSession;
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

            return ResponseEntity.ok("이미지 등록에 성공했습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("이미지 등록에 실패했습니다.");
        }
    }

    // 연습용 코드
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "No file selected";
        }

        try {
            String fileName = file.getOriginalFilename();
            s3Client.putObject(bucketName, fileName, file.getInputStream(), null);
            return "File uploaded: " + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return "Upload failed";
        }
    }
}
