package kopo.poly.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.auth.AuthInfo;
import kopo.poly.dto.*;
import kopo.poly.repository.UserFollowRepository;
import kopo.poly.repository.UserInfoRepository;
import kopo.poly.repository.UserInterestsRepository;
import kopo.poly.repository.entity.FollowPK;
import kopo.poly.repository.entity.UserFollowEntity;
import kopo.poly.repository.entity.UserInfoEntity;
import kopo.poly.repository.entity.UserInterestsEntity;
import kopo.poly.service.IMailService;
import kopo.poly.service.IUserInfoService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.DateUtil;
import kopo.poly.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service("UserInfoService")
public class UserInfoService implements IUserInfoService {

    private final UserInfoRepository userInfoRepository;
    private final UserFollowRepository userFollowRepository;
    private final UserInterestsRepository userInterestsRepository;
    private final IMailService mailService;

    /**
     * 아이디 중복 체크
     *
     * @param pDTO 회원 가입을 위한 아이디
     * @return 아이디 중복 여부 결과
     */
    @Override
    public UserInfoDTO getUserIdExists(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".getUserIdExists Start!");

        AtomicReference<UserInfoDTO> atomicReference = new AtomicReference<>(); // 람다로 인해 값을 공유하지 못하여 AtomicReference 사용함

        // ifPresentOrElse 값이 존재할 떄와 값이 존재 안할 때, 수행할 내용을 정의(람다 표현식 사용)
        userInfoRepository.findByUserId(pDTO.userId()).ifPresentOrElse(entity -> {
            atomicReference.set(UserInfoDTO.builder().existsYn("Y").build()); // 객체에 값이 존재한다면...

        }, () -> {
            atomicReference.set(UserInfoDTO.builder().existsYn("N").build()); // 값이 존재하지 않는다면...

        });

        log.info(this.getClass().getName() + ".getUserIdExists End!");

        return atomicReference.get();
    }


    /**
     * 회원 가입하기(회원정보 등록하기)
     *
     * @param pDTO 회원 가입을 위한 회원정보
     * @return 회원가입 결과
     */
    @Override
    public int insertUserInfo(UserInfoDTO pDTO) {

        log.info(this.getClass().getName() + ".insertUserInfo Start!");

        int res = 0; // 회원가입 성공 : 1, 아이디 중복으로인한 가입 취소 : 2, 기타 에러 발생 : 0

        log.info("pDTO : " + pDTO);

        // 회원 가입 중복 방지를 위해 DB에서 데이터 조회
        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(pDTO.userId());
        Optional<UserInfoEntity> emailCheckEntity = userInfoRepository.findByEmail(pDTO.email());

        if (rEntity.isPresent()) {
            res = 2;
        } else if (emailCheckEntity.isPresent()) {
            res = 3;
        } else {

            log.info("imageUrl : " + pDTO.profilePath());

            UserInfoEntity pEntity = UserInfoDTO.of(pDTO);

            // 회원정보 DB에 저장
            userInfoRepository.save(pEntity);

            rEntity = userInfoRepository.findByUserId(pDTO.userId());

            if (rEntity.isPresent()) {
                res = 1;
            } else {
                res = 0;
            }
        }

        log.info(this.getClass().getName() + ".insertUserInfo End!");
        return res;
    }

    /**
     * @param userId   회원 아이디
     * @param keywords 해당 회원이 입력한 키워드
     */
    @Override
    @Transactional
    public void saveKeywordsForUser(String userId, List<String> keywords) throws Exception {
        log.info(this.getClass().getName() + ".saveKeywordsForUser Start!");

        for (String keyword : keywords) {
            UserInterestsEntity uEntity = UserInterestsEntity.builder()
                    .userId(userId)
                    .keyword(keyword)
                    .build();

            userInterestsRepository.save(uEntity);
        }

        log.info(this.getClass().getName() + ".saveKeywordsForUser End!");
    }

    @Override
    public List<UserInterestsDTO> getKeywordList(String userId) {

        log.info(this.getClass().getName() + ".getKeywordList Start!");

        List<UserInterestsEntity> rEntity = userInterestsRepository.findByUserId(userId);

        List<UserInterestsDTO> rList = new ObjectMapper().convertValue(rEntity,
                new TypeReference<List<UserInterestsDTO>>() {
                });

        log.info(this.getClass().getName() + ".getKeywordList End!");

        return rList;
    }

    @Override
    @Transactional
    public void updateKeywords(String userId, List<String> keywords) throws Exception {
        log.info(this.getClass().getName() + ".updateKeywordsForUser Start!");

        // 기존 키워드 삭제
        userInterestsRepository.deleteByUserId(userId);

        // 새 키워드 추가
        List<UserInterestsEntity> newInterests = keywords.stream()
                .map(keyword -> UserInterestsEntity.builder()
                        .userId(userId)
                        .keyword(keyword)
                        .build())
                .collect(Collectors.toList());

        userInterestsRepository.saveAll(newInterests);

        log.info(this.getClass().getName() + ".updateKeywordsForUser End!");
    }

    /**
     * 회원탈퇴
     *
     * @param pDTO 회원정보
     */
    @Override
    public void deleteUserProc(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".deleteUserProc Start!");

        userInfoRepository.deleteById(pDTO.userId());

        log.info(this.getClass().getName() + ".deleteUserProc End!");

    }

    /**
     * 로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기
     *
     * @param pDTO 로그인을 위한 회원정보
     * @return 회원가입 결과
     */
    @Override
    public int getUserLogin(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".getUserLogin Start!");

        int res = 0;

        String userId = CmmUtil.nvl(pDTO.userId());
        String password = CmmUtil.nvl(pDTO.password());

        log.info("userId : " + userId);
        log.info("password : " + password);

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserIdAndPassword(userId, password);

        if (rEntity.isPresent()) {
            res = 1;
        }

        log.info(this.getClass().getName() + ".getUserLogin End!");

        return res;
    }

    /**
     * 특정 유저 전체 정보 조회
     *
     * @param userId 유저 아이디
     * @return 정보 조회 결과
     */
    @Override
    public UserInfoDTO getUserInfo(String userId) throws Exception {

        log.info(this.getClass().getName() + "getUserInfo Start!");

        // SELECT * FROM USER_INFO WHERE USER_ID = 'hglee67' 쿼리 실행과 동일
        UserInfoDTO rDTO = UserInfoDTO.from(userInfoRepository.findByUserId(userId).orElseThrow());

        log.info(this.getClass().getName() + "getUserInfo End!");

        return rDTO;
    }

    /**
     * 아이디 찾기
     *
     * @param pDTO 회원정보
     * @return 아이디 찾기 결과
     */
    @Override
    public String searchUserIdProc(UserInfoDTO pDTO) throws Exception {
        log.info(this.getClass().getName() + ".searchUserIdProc Start!");

        String res = "";

        String userName = CmmUtil.nvl(pDTO.userName());
        String email = CmmUtil.nvl(pDTO.email());

        log.info("pDTO userName : " + userName);
        log.info("pDTO email : " + email);

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserNameAndEmail(pDTO.userName(), pDTO.email());

        log.info("rEntity : " + rEntity);

        if (rEntity.isPresent()) {
            res = rEntity.get().getUserId();
        }

        log.info(this.getClass().getName() + ".searchUserIdProc End!");

        return res;
    }

    /**
     * 이메일 중복체크
     *
     * @param pDTO 회원정보
     * @return 중복체크 결과
     */
    @Override
    public UserInfoDTO getEmailExists(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".getEmailExists Start!");

        UserInfoDTO rDTO;

        String email = CmmUtil.nvl(pDTO.email());
        String existsYn;

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByEmail(email);

        if (rEntity.isPresent()) {
            existsYn = "Y";
        } else {
            existsYn = "N";
        }

        log.info("existsYn : " + existsYn);

        UserInfoDTO.UserInfoDTOBuilder builder = UserInfoDTO.builder().existsYn(existsYn);

        if (existsYn.equals("N")) {

            // 6자리 랜덤 숫자 생성하기
            int authNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);

            log.info("authNumber : " + authNumber);

            builder.authNumber(authNumber);

            // 인증번호 발송 로직
            MailDTO dto = MailDTO.builder()
                    .title("인증번호 발송 메일")
                    .contents("인증번호는 " + authNumber + " 입니다.")
                    .toMail(EncryptUtil.decAES128CBC(email))
                    .build();

            mailService.doSendMail(dto); // 이메일 발송

        }

        rDTO = builder.build();

        log.info(this.getClass().getName() + ".getEmailExists End!");

        return rDTO;
    }

    /**
     * 비밀번호 찾기
     *
     * @param pDTO 회원정보
     * @return 비밀번호 찾기 결과
     */
    @Override
    public UserInfoDTO searchPasswordProc(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".searchPasswordProc Start!");

        String userId = CmmUtil.nvl(pDTO.userId());
        String email = CmmUtil.nvl(pDTO.email());
        String userName = CmmUtil.nvl(pDTO.userName());

        log.info("pDTO userId : " + userId);
        log.info("pDTO userName : " + userName);
        log.info("pDTO email : " + email);

        UserInfoEntity rEntity = userInfoRepository.findByUserIdAndEmailAndUserName(userId, email, userName);

        UserInfoDTO rDTO = new ObjectMapper().convertValue(rEntity, UserInfoDTO.class);

        log.info("rDTO : " + rDTO);

        if (rDTO != null) {
            log.info("rDTO userId : " + rDTO.userId());
            log.info("rDTO userName : " + rDTO.userName());
            log.info("rDTO email: " + rDTO.email());
            log.info("rDTO password: " + rDTO.password());
        }

        log.info(this.getClass().getName() + ".searchPasswordProc End!");

        return rDTO;

    }

    /**
     * 닉네임 변경
     *
     * @param pDTO 회원정보
     */
    @Transactional
    @Override   // 닉네임 변경 함수
    public void newUserNameProc(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + "changeUserName start!");

        log.info("userName : " + pDTO.userName());

        String userId = CmmUtil.nvl(pDTO.userId());

        Optional<UserInfoEntity> uEntity = userInfoRepository.findByUserId(userId);

        if (uEntity.isPresent()) {

            UserInfoEntity rEntity = uEntity.get();

            UserInfoEntity pEntity = UserInfoEntity.builder()
                    .userId(rEntity.getUserId()).userName(pDTO.userName())
                    .password(rEntity.getPassword())
                    .email(rEntity.getEmail())
                    .regId(rEntity.getUserId()).regDt(rEntity.getRegDt())
                    .chgId(rEntity.getUserId()).chgDt(DateUtil.getDateTime("yyyy-MM-dd hh:mm:ss"))
                    .provider(rEntity.getProvider())
                    .profilePath(rEntity.getProfilePath())
                    .build();

            userInfoRepository.save(pEntity);

        }

        log.info(this.getClass().getName() + "changeUserName end!");

    }

    /**
     * 비밀번호 변경
     *
     * @param pDTO 회원정보
     */
    @Transactional
    @Override
    public void newPasswordProc(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + "newPasswordProc start!");

        String userId = CmmUtil.nvl(pDTO.userId());

        // 사용자 ID로 사용자 엔티티 조회
        Optional<UserInfoEntity> uEntity = userInfoRepository.findByUserId(userId);

        if (uEntity.isPresent()) {

            UserInfoEntity rEntity = uEntity.get();

            log.info("rEntity userId : " + rEntity.getUserId());
            log.info("rEntity password : " + rEntity.getPassword());
            log.info("rEntity userName : " + rEntity.getUserName());
            log.info("rEntity email : " + rEntity.getEmail());

            UserInfoEntity pEntity = UserInfoEntity.builder()
                    .userId(rEntity.getUserId()).userName(rEntity.getUserName())
                    .password(pDTO.password())
                    .email(rEntity.getEmail())
                    .regId(rEntity.getUserId()).regDt(rEntity.getRegDt())
                    .chgId(rEntity.getUserId()).chgDt(DateUtil.getDateTime("yyyy-MM-dd hh:mm:ss"))
                    .build();

            userInfoRepository.save(pEntity);

        }

        log.info(this.getClass().getName() + "newPasswordProc End!");
    }

    /**
     * 프로필 사진 등록
     *
     * @param pDTO 회원정보
     */
    @Transactional
    @Override
    public void profilePathProc(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + "profilePathProc start!");

        String userId = CmmUtil.nvl(pDTO.userId());

        // 사용자 ID로 사용자 엔티티 조회
        Optional<UserInfoEntity> uEntity = userInfoRepository.findByUserId(userId);

        if (uEntity.isPresent()) {

            UserInfoEntity rEntity = uEntity.get();

            UserInfoEntity pEntity = UserInfoEntity.builder()
                    .userId(rEntity.getUserId()).userName(rEntity.getUserName())
                    .password(rEntity.getPassword())
                    .email(rEntity.getEmail())
                    .regId(rEntity.getUserId()).regDt(rEntity.getRegDt())
                    .chgId(rEntity.getUserId()).chgDt(DateUtil.getDateTime("yyyy-MM-dd hh:mm:ss"))
                    .provider(rEntity.getProvider())
                    .profilePath(pDTO.profilePath())
                    .build();

            userInfoRepository.save(pEntity);

        } else {
            log.error("No user found with ID: " + userId);
            throw new RuntimeException("User not found");
        }

        log.info(this.getClass().getName() + "profilePathProc End!");

    }

    /**
     * 팔로우하기
     *
     * @param pDTO 회원정보
     */
    @Override
    public void addFollower(UserFollowDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".addFollower Start!");

        UserFollowEntity pEntity = UserFollowEntity.builder()
                .followingId(pDTO.followingId())
                .followerId(pDTO.followerId())
                .regDt(DateUtil.getDateTime("yyyy-MM-dd hh:mm:ss").substring(0, 10))
                .build();

        log.info(this.getClass().getName() + ".addFollower End!");

        userFollowRepository.save(pEntity);

    }

    /**
     * 팔로우 취소하기
     *
     * @param pDTO 회원정보
     */
    @Override
    public void removeFollower(UserFollowDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".removeFollower Start!");

        String followingId = pDTO.followingId();
        String followerId = pDTO.followerId();

        log.info("followingId : " + followingId);
        log.info("followerId : " + followerId);

        FollowPK followPK = FollowPK.builder()
                .followingId(followingId)
                .followerId(followerId)
                .build();

        userFollowRepository.deleteById(followPK);

        log.info(this.getClass().getName() + ".removeFollower End!");
    }

    /**
     * 팔로우 여부 조회
     *
     * @param pDTO 회원정보
     * @return 팔로우 여부 조회 결과
     */
    @Override
    public boolean getFollowInfo(UserFollowDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".getFollowInfo Start!");

        String followingId = CmmUtil.nvl(pDTO.followingId());
        String followerId = CmmUtil.nvl(pDTO.followerId());

        boolean isFollowing = userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId);

        log.info("isFollowing : " + isFollowing);

        log.info(this.getClass().getName() + ".getFollowInfo End!");

        return isFollowing;
    }

    /**
     * 팔로워 수 조회
     *
     * @param followerId 회원 아이디
     * @return 팔로워 수 조회 결과
     */
    @Override
    public long countByFollowerId(String followerId) throws Exception {

        return userFollowRepository.countByFollowerId(followerId);
    }

    /**
     * 팔로잉 수 조회
     *
     * @param followingId 회원 아이디
     * @return 팔로잉 수 조회 결과
     */
    @Override
    public long countByFollowingId(String followingId) throws Exception {

        return userFollowRepository.countByFollowingId(followingId);
    }

    /**
     * 팔로우 리스트 조회
     *
     * @param userId 회원 아이디
     * @return 팔로우 리스트 조회 결과
     */
    @Override
    public List<UserFollowDTO> getFollowList(String userId) throws Exception {

        log.info(this.getClass().getName() + ".getFollowList Start!");

        // 현재 사용자가 팔로우하는 사용자 ID 리스트를 가져옵니다.
        List<UserFollowEntity> userFollows = userFollowRepository.findByFollowerId(userId);

        List<String> followingIds = userFollowRepository.findByFollowerId(userId)
                .stream()
                .map(UserFollowEntity::getFollowingId)
                .collect(Collectors.toList());

        log.info("followingIds : " + followingIds);

        List<UserFollowDTO> nList = userFollows.stream()
                .filter(e -> followingIds.contains(e.getFollowingId())) // 팔로잉 목록에 포함된 ID만 필터링
                .map(e -> {
                    UserFollowDTO rDTO = UserFollowDTO.builder()
                            .followingId(e.getFollowingId())
                            .regDt(e.getRegDt())
                            .userId(e.getUserInfoFollowing().getUserId())
                            .userName(e.getUserInfoFollowing().getUserName())
                            .profilePath(e.getUserInfoFollowing().getProfilePath())
                            .build();
                    return rDTO;
                }).distinct() // 중복 제거
                .collect(Collectors.toList());

        log.info(this.getClass().getName() + ".getFollowList End!");

        return nList;
    }

    /**
     * 팔로잉 리스트 조회
     *
     * @param userId 회원 아이디
     * @return 팔로잉 리스트 조회 결과
     */
    @Override
    public List<UserFollowDTO> getFollowingList(String userId) throws Exception {

        log.info(this.getClass().getName() + ".getFollowingList Start!");

        // 현재 사용자가 팔로워인 모든 사용자의 팔로우 엔티티를 가져옵니다.
        List<UserFollowEntity> userFollows = userFollowRepository.findByFollowingId(userId);

        // 스트림을 사용하여 팔로워 정보를 UserFollowDTO로 변환합니다.
        List<UserFollowDTO> nList = userFollows.stream()
                .map(e -> UserFollowDTO.builder()
                        .followerId(e.getFollowerId())
                        .regDt(e.getRegDt())
                        .userId(e.getUserInfoFollower().getUserId())
                        .userName(e.getUserInfoFollower().getUserName())
                        .profilePath(e.getUserInfoFollower().getProfilePath())
                        .build())
                .distinct() // 중복 제거
                .collect(Collectors.toList());

        log.info(this.getClass().getName() + ".getFollowingList End!");

        return nList;

    }

    /**
     * 팔로우 유저들 게시글 조회
     *
     * @param userId 회원 아이디
     * @return 팔로우 유저들 게시글 조회 결과
     */
    @Override
    public List<String> noticeFollowList(String userId) throws Exception {

        log.info(this.getClass().getName() + ".noticeFollowList Start!");

        // 현재 사용자가 팔로우하는 사용자 ID 리스트를 가져옵니다.
        List<UserFollowEntity> userFollows = userFollowRepository.findByFollowerId(userId);

        log.info("userFollows : " + userFollows);

        List<String> followingList = userFollows.stream() // 북마크 데이터에서 eventSeq들만 추출하는 과정
                .map(UserFollowEntity::getFollowingId)
                .collect(Collectors.toList());

        log.info(this.getClass().getName() + ".noticeFollowList End!");

        return followingList;
    }

    /**
     * userList에 있는 유저들의 정보 가져오기
     *
     * @param userList 유저 목록
     * @return 리스트에 있는 유저들의 정보
     */
    @Override
    public List<UserInfoDTO> getUserInfoList(List<String> userList) throws Exception {

        log.info(this.getClass().getName() + ".getUserInfoList Start!");

        List<UserInfoEntity> userInfoEntityList = userInfoRepository.findByUserIdIn(userList);

        List<UserInfoDTO> userInfoList = userInfoEntityList.stream()
                .map(user -> UserInfoDTO.builder().userId(user.getUserId()).userName(user.getUserName()).profilePath(user.getProfilePath())
                        .build()).collect(Collectors.toList());

        log.info(this.getClass().getName() + ".getUserInfoList End!");

        return userInfoList;
    }

    /**
     * Spring Security에서 로그인 처리를 하기 위해 실행하는 함수
     * Spring Security의 인증 기능을 사용하기 위해선 반드시 만들어야 하는 함수
     * <p>
     * Controller로부터 호출되지않고, Spring Security가 바로 호출함
     * <p>
     * 아이디로 검색하고, 검색한 결과를 기반으로 Spring Security가 비밀번호가 같은지 판단함
     * <p>
     * 아이디와 패스워드가 일치하지 않으면 자동으로 UsernameNotFoundException 발생시킴
     *
     * @param userId 사용자 아이디
     */

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        log.info(this.getClass().getName() + ".loadUserByUsername Start!");

        log.info("userId : " + userId);

        // 로그인 요청한 사용자 아이디를 검색함
        // SELECT * FROM USER_INFO WHERE USER_ID = 'hglee67'
        UserInfoEntity rEntity = userInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException(userId + " Not Found User"));

        // rEntity 데이터를 DTO로 변환하기
        UserInfoDTO rDTO = null;
        try {
            rDTO = UserInfoDTO.from(rEntity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 비밀번호가 맞는지 체크 및 권한 부여를 위해 rDTO를 UserDetails를 구현한 AuthInfo에 넣어주기
        return new AuthInfo(rDTO);
    }

}
