package kopo.poly.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kopo.poly.repository.entity.UserInfoEntity;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.DateUtil;
import kopo.poly.util.EncryptUtil;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserInfoDTO(

        String userId, // 회원아이디
        String userName, // 닉네임

        @NotBlank(message = "비밀번호는 필수 입력 사항입니다.")
        @Size(max = 16, message = "비밀번호는 16글자까지 입력가능합니다.")
        String password, // 비밀번호

        @NotBlank(message = "이메일은 필수 입력 사항입니다.")
        @Size(max = 30, message = "이메일은 30글자까지 입력가능합니다.")
        String email, // 이메일
        String regId, // 등록자아이디
        String regDt, // 등록일시
        String chgId, // 최근 수정자아이디
        String chgDt, // 최근
        MultipartFile profileImage, // 파일을 추가
        String profilePath, // 프로필 사진 경로
        Integer authNumber, // 이메일 중복체크를 위한 인증번호

        // OAuth2 추가 필드
        String provider, // 로그인 제공자
//        String providerId  제공자 고유 아이디

        // 회원가입시, 중복가입을 방지 위해 사용할 변수
        // DB를 조회해서 회원이 존재하면 Y값을 반환함
        // DB테이블에 존재하지 않는 가상의 컬럼(ALIAS)
        String existsYn) implements Serializable {

    /**
     * 패스워드, 권한 등 회원 가입을 위한 정보 만들기
     */
    public static UserInfoDTO createUser(UserInfoDTO pDTO, String password, String imageUrl) throws Exception {

        UserInfoDTO rDTO = UserInfoDTO.builder()
                .userId(pDTO.userId())
                .userName(pDTO.userName())
                .password(password) // Spring Security 생성해준 암호화된 비밀번호
                .email(EncryptUtil.encAES128CBC(pDTO.email()))
                .regId(pDTO.userId())
                .chgId(pDTO.userId())
                .profilePath(imageUrl)
                .build();

        return rDTO;
    }

    /**
     * DTO 결과를 entity 변환하기
     * 이전 실습에서 진행한 Jackson 객체를 통해 처리도 가능함
     */
    public static UserInfoEntity of(UserInfoDTO dto) {

        UserInfoEntity entity = UserInfoEntity.builder()
                .userId(dto.userId())
                .userName(dto.userName())
                .password(dto.password())
                .email(dto.email())
                .regId(dto.regId())
                .regDt(dto.regDt())
                .chgId(dto.chgId())
                .profilePath(dto.profilePath())
                .chgDt(dto.chgDt()).build();

        return entity;
    }

    /**
     * JPA로 전달받은 entity 결과를 DTO로 변환하기
     * 이전 실습에서 진행한 Jackson 객체를 통해 처리도 가능함
     */
    public static UserInfoDTO from(UserInfoEntity entity) throws Exception {

        UserInfoDTO rDTO = UserInfoDTO.builder()
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .password(entity.getPassword())
                .email(EncryptUtil.decAES128CBC(CmmUtil.nvl(entity.getEmail())))
                .regId(entity.getRegId())
                .regDt(entity.getRegDt())
                .chgId(entity.getChgId())
                .profilePath(entity.getProfilePath())
                .chgDt(entity.getChgDt()).build();

        return rDTO;
    }
}