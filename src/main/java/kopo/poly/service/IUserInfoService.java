package kopo.poly.service;

import kopo.poly.dto.BookmarkDTO;
import kopo.poly.dto.UserFollowDTO;
import kopo.poly.dto.UserInfoDTO;
import kopo.poly.dto.UserInterestsDTO;

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
     * 키워드 추가 로직
     *
     * @param userId 회원 아이디
     * @param keywords 해당 회원이 입력한 키워드
     */
    void saveKeywordsForUser(String userId, List<String> keywords) throws Exception;

    /**
     * 키워드 검색 로직
     *
     * @param userId 회원 아이디
     * @return 해당 회원의 키워드 리스트
     */

    List<UserInterestsDTO> getKeywordList(String userId) throws Exception;
    /**
     * 키워드 수정 로직
     *
     * @param userId 회원 아이디
     */
    void updateKeywords(String userId, List<String> keywords) throws Exception;

    /**
     * 회원탈퇴
     *
     * @param pDTO 회원정보
     */
    void deleteUserProc(UserInfoDTO pDTO) throws Exception;

    /**
     *  로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기
     *
     * @param pDTO 로그인을 위한 회원정보
     * @return 회원가입 결과
     */
    int getUserLogin(UserInfoDTO pDTO) throws Exception;

    /**
     * 특정 유저 전체 정보 조회
     *
     * @param userId 유저 아이디
     * @return 정보 조회 결과
     */
    UserInfoDTO getUserInfo(String userId) throws Exception;

    /**
     * 아이디 찾기
     *
     * @param pDTO 회원정보
     * @return 아이디 찾기 결과
     */
    String searchUserIdProc(UserInfoDTO pDTO) throws Exception;

    /**
     * 이메일 중복체크
     *
     * @param pDTO 회원정보
     * @return 중복체크 결과
     */
    UserInfoDTO getEmailExists(UserInfoDTO pDTO) throws Exception;

    /**
     * 비밀번호 찾기
     *
     * @param pDTO 회원정보
     * @return 비밀번호 찾기 결과
     */
    UserInfoDTO searchPasswordProc(UserInfoDTO pDTO) throws Exception;

    /**
     * 닉네임 변경
     *
     * @param pDTO 회원정보
     */
    void newUserNameProc(UserInfoDTO pDTO) throws Exception;

    /**
     * 비밀번호 변경
     *
     * @param pDTO 회원정보
     */
    void newPasswordProc(UserInfoDTO pDTO) throws Exception;

    /**
     * 프로필 사진 등록
     *
     * @param pDTO 회원정보
     */
    void profilePathProc(UserInfoDTO pDTO) throws Exception;

    /**
     * 팔로우하기
     *
     * @param pDTO 회원정보
     */
    void addFollower(UserFollowDTO pDTO) throws Exception;

    /**
     * 팔로우 취소하기
     *
     * @param pDTO 회원정보
     */
    void removeFollower(UserFollowDTO pDTO) throws Exception;

    /**
     * 팔로우 여부 조회
     *
     * @param pDTO 회원정보
     * @return 팔로우 여부 조회 결과
     */
    boolean getFollowInfo(UserFollowDTO pDTO) throws Exception;

    /**
     * 팔로워 수 조회
     *
     * @param followerId 회원 아이디
     * @return 팔로워 수 조회 결과
     */
    long countByFollowerId(String followerId) throws Exception;

    /**
     * 팔로잉 수 조회
     *
     * @param followingId 회원 아이디
     * @return 팔로잉 수 조회 결과
     */
    long countByFollowingId(String followingId) throws Exception;

    /**
     * 팔로우 리스트 조회
     *
     * @param userId 회원 아이디
     * @return 팔로우 리스트 조회 결과
     */
    List<UserFollowDTO> getFollowList(String userId) throws Exception;

    /**
     * 팔로잉 리스트 조회
     *
     * @param userId 회원 아이디
     * @return 팔로잉 리스트 조회 결과
     */
    List<UserFollowDTO> getFollowingList(String userId) throws Exception;

    /**
     * 팔로우 유저들 게시글 조회
     *
     * @param userId 회원 아이디
     * @return 팔로우 유저들 게시글 조회 결과
     */
    List<String> noticeFollowList(String userId) throws Exception;

}
