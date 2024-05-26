package kopo.poly.config;

import jakarta.servlet.http.HttpSession;
import kopo.poly.service.impl.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, ClientRegistrationRepository clientRegistrationRepository) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .anyRequest().permitAll() // 모든 경로에 대해 접근 허용
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/user/login") // 사용자 정의 로그인 페이지 경로 지정
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 사용자 정의 OAuth2UserService 지정
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler()) // 인증 성공 후 핸들러 지정
                )
                .formLogin(form -> form
                        .loginPage("/user/login") // 폼 기반 로그인 활성화 및 로그인 페이지 지정
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler()) // OAuth2 로그아웃 핸들러 지정
                        .addLogoutHandler(new SecurityContextLogoutHandler()) // 세션 및 인증 정보 정리
                        .permitAll()
                )
                .csrf().disable(); // CSRF 보호 비활성화 (필요에 따라 설정)
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            HttpSession session = request.getSession();
            DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
            String userId = oauthUser.getAttribute("email");

            session.setAttribute("SS_USER_ID", userId);

            response.sendRedirect("/main");
        };
    }

    @Bean
    public LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);

        // 로그아웃 성공 후 리디렉트할 URL 설정
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("https://culturalsquare.site/user/login");

        return oidcLogoutSuccessHandler;
    }
}
