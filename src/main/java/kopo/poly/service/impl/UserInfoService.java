package kopo.poly.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kopo.poly.dto.*;
import kopo.poly.repository.UserFollowRepository;
import kopo.poly.repository.UserInfoRepository;
import kopo.poly.repository.entity.*;
import kopo.poly.service.IMailService;
import kopo.poly.service.IUserInfoService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.DateUtil;
import kopo.poly.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service("UserInfoService")
public class UserInfoService implements IUserInfoService {

    private final UserInfoRepository userInfoRepository;
    private final UserFollowRepository userFollowRepository;

    private final IMailService mailService;
    private final JPAQueryFactory queryFactory;

    @Override
    public UserInfoDTO getUserIdExists(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".getUserIdExists Start!");

        UserInfoDTO rDTO;

        String userId = CmmUtil.nvl(pDTO.userId());

        log.info("userId : " + userId);

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        if (rEntity.isPresent()) {
            rDTO = UserInfoDTO.builder().userId(rEntity.get().getUserId()).userName(rEntity.get().getUserName()).existsYn("Y").build();
        } else {
            rDTO = UserInfoDTO.builder().existsYn("N").build();
        }

        log.info(this.getClass().getName() + ".getUserIdExists End!");

        return rDTO;
    }

    @Override
    public int insertUserInfo(UserInfoDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".insertUserInfo Start!");

        int res = 0;

        String userId = CmmUtil.nvl(pDTO.userId());
        String userName = CmmUtil.nvl(pDTO.userName());
        String password = CmmUtil.nvl(pDTO.password());
        String email = CmmUtil.nvl(pDTO.email());

        log.info("pDTO : " + pDTO);

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        if (rEntity.isPresent()) {
            res = 2;
        } else {
            UserInfoEntity pEntity = UserInfoEntity.builder()
                    .userId(userId).userName(userName)
                    .password(password)
                    .email(email)
                    .regId(userId).regDt(DateUtil.getDateTime("yyyy-MM-dd hh:mm:ss"))
                    .chgId(userId).chgDt(DateUtil.getDateTime("yyyy-MM-dd hh:mm:ss"))
                    .build();

            userInfoRepository.save(pEntity);

            rEntity = userInfoRepository.findByUserId(userId);

            if (rEntity.isPresent()) {
                res = 1;
            } else {
                res = 0;
            }
        }

        log.info(this.getClass().getName() + ".insertUserInfo End!");
        return res;
    }

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

    @Override
    public UserInfoDTO getUserInfo(String userId) throws Exception {

        log.info(this.getClass().getName() + "getUserInfo Start!");

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        UserInfoDTO rDTO;

        if (rEntity.isPresent()) {

            UserInfoEntity pEntity = rEntity.get();

            rDTO = UserInfoDTO.builder()
                    .userId(pEntity.getUserId())
                    .userName(pEntity.getUserName())
                    .profilePath(pEntity.getProfilePath())
                    .regDt(pEntity.getRegDt())
                    .build();
        } else rDTO = UserInfoDTO.builder().build();

        log.info(this.getClass().getName() + "getUserInfo End!");

        return rDTO;
    }

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

//        if (rEntity.isPresent()) {
//            rDTO = new ObjectMapper().convertValue(rEntity.get(), UserInfoDTO.class);
//        } else {
//            rDTO = UserInfoDTO.builder().build();
//        }

        if (rEntity.isPresent()) {
            res = rEntity.get().getUserId();
        }

        log.info(this.getClass().getName() + ".searchUserIdProc End!");

        return res;
    }

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
                    .profilePath(rEntity.getProfilePath())
                    .build();

            userInfoRepository.save(pEntity);

        }

        log.info(this.getClass().getName() + "changeUserName end!");

    }

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
                    .profilePath(pDTO.profilePath())
                    .build();

            userInfoRepository.save(pEntity);

        } else {
            log.error("No user found with ID: " + userId);
            throw new RuntimeException("User not found");
        }

        log.info(this.getClass().getName() + "profilePathProc End!");

    }

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

    @Override
    public void removeFollower(UserFollowDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".removeFollower Start!");

        String followingId = pDTO.followingId();
        String followerId = pDTO.followerId();

        log.info("followingId : " + followingId);
        log.info("followerId : " + followerId);

        FollowKey followKey = FollowKey.builder()
                .followingId(followingId)
                .followerId(followerId)
                .build();

        userFollowRepository.deleteById(followKey);

        log.info(this.getClass().getName() + ".removeFollower End!");
    }

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

}
