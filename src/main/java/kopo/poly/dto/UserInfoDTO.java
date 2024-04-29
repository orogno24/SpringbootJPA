package kopo.poly.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record UserInfoDTO(

        String userId, // 회원아이디
        String userName, // 제목
        String password, // 비밀번호
        String email, // 이메일
        String regId, // 등록자아이디
        String regDt, // 등록일시
        String chgId, // 최근 수정자아이디
        String chgDt, // 최근 수정일시
        String existsYn, // 회원아이디 존재여부
        String profilePath, // 프로필 사진 경로

        // 이메일 중복체크를 위한 인증번호
        int authNumber,
        int mailNumber

) {

}