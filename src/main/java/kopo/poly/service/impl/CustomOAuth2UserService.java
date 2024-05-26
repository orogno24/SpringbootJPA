package kopo.poly.service.impl;

import kopo.poly.repository.UserInfoRepository;
import kopo.poly.repository.entity.UserInfoEntity;
import kopo.poly.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
@Slf4j
@RequiredArgsConstructor
@Service("CustomOAuth2UserService")
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserInfoRepository userInfoRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        log.info("OAuth2UserRequest: {}", userRequest);
        log.info("Client Registration: {}", userRequest.getClientRegistration());
        log.info("Access Token: {}", userRequest.getAccessToken().getTokenValue());
        log.info("Redirect URI: {}", userRequest.getClientRegistration().getRedirectUri());

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String provider = userRequest.getClientRegistration().getRegistrationId();
//        String providerId = oAuth2User.getName();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String profilePath = (String) attributes.get("picture");

        UserInfoEntity pEntity = UserInfoEntity.builder()
                .userId(email) // userId를 email로 설정
                .userName(name)
                .email(email)
                .profilePath(profilePath)
                .provider(provider)
//                .providerId(providerId)
                .build();

        saveOrUpdateUser(pEntity);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "name");
    }

    private void saveOrUpdateUser(UserInfoEntity pEntity) {
        Optional<UserInfoEntity> rEntityOptional = userInfoRepository.findByEmail(pEntity.getEmail());
        if (rEntityOptional.isPresent()) {
            UserInfoEntity existingUser = rEntityOptional.get();

            // 기존 사용자 정보를 유지하면서 소셜 로그인 정보로 업데이트
            UserInfoEntity updatedUser = UserInfoEntity.builder()
                    .userId(existingUser.getUserId()) // 기존 userId 유지
                    .userName(pEntity.getUserName()) // 소셜 로그인에서 가져온 사용자명으로 업데이트
                    .email(existingUser.getEmail()) // 기존 이메일 유지
                    .provider(pEntity.getProvider()) // 소셜 로그인 제공자 정보 업데이트
//                    .providerId(pEntity.getProviderId()) // 소셜 로그인 제공자 ID 업데이트
                    .regId(existingUser.getRegId()) // 기존 등록자 ID 유지
                    .regDt(existingUser.getRegDt()) // 기존 등록일시 유지
                    .chgId(existingUser.getUserId()) // 현재 사용자로 변경 ID 설정
                    .chgDt(DateUtil.getDateTime("yyyy-MM-dd hh:mm:ss")) // 현재 시각으로 변경일시 설정
                    .profilePath(pEntity.getProfilePath()) // 소셜 로그인에서 가져온 프로필 사진 URL로 업데이트
                    .build();

            userInfoRepository.save(updatedUser);
        } else {
            userInfoRepository.save(pEntity);
        }
    }
}