package kopo.poly.service;

import kopo.poly.dto.BookmarkDTO;
import kopo.poly.dto.UserFollowDTO;
import kopo.poly.dto.UserInfoDTO;

import java.util.List;

public interface IUserInfoService {

    /**
     * 아이디 중복 체크
     *
     * @param pDTO 회원 가입을 위한 아이디
     * @return 아이디 중복 여부 결과
     */
    UserInfoDTO getUserIdExists(UserInfoDTO pDTO) throws Exception;

    /**
     * 회원 가입하기(회원정보 등록하기)
     *
     * @param pDTO 회원 가입을 위한 회원정보
     * @return 회원가입 결과
     */
    int insertUserInfo(UserInfoDTO pDTO) throws Exception;

    /**
     *  로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기
     *
     * @param pDTO 로그인을 위한 회원정보
     * @return 회원가입 결과
     */
    int getUserLogin(UserInfoDTO pDTO) throws Exception;

    // 특정 유저 전체 정보 조회 코드
    UserInfoDTO getUserInfo(String userId) throws Exception;

    // 아이디찾기
    String searchUserIdProc(UserInfoDTO pDTO) throws Exception;

    // 이메일 중복체크
    UserInfoDTO getEmailExists(UserInfoDTO pDTO) throws Exception;

    // 비밀번호찾기
    UserInfoDTO searchPasswordProc(UserInfoDTO pDTO) throws Exception;

    // 닉네임변경
    void newUserNameProc(UserInfoDTO pDTO) throws Exception;

    // 비밀번호변경
    void newPasswordProc(UserInfoDTO pDTO) throws Exception;

    // 프로필 사진 등록
    void profilePathProc(UserInfoDTO pDTO) throws Exception;

    // 팔로우하기
    void addFollower(UserFollowDTO pDTO) throws Exception;

    // 팔로우 취소하기
    void removeFollower(UserFollowDTO pDTO) throws Exception;

    // 팔로우 여부 조회
    boolean getFollowInfo(UserFollowDTO pDTO) throws Exception;

    //팔로워 수 조회
    long countByFollowerId(String followerId) throws Exception;

    //팔로잉 수 조회
    long countByFollowingId(String followingId) throws Exception;

    // 팔로우 리스트 조회
    List<UserFollowDTO> getFollowList(String userId) throws Exception;

    // 팔로잉 리스트 조회
    List<UserFollowDTO> getFollowingList(String userId) throws Exception;

}
