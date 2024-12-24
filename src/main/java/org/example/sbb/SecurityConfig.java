package org.example.sbb;

import lombok.RequiredArgsConstructor;
import org.example.sbb.user.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize 사용을 위해 필수
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService; // OAuth2UserService 주입

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(new AntPathRequestMatcher("/**")).permitAll()) // 모든 요청 허용
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))) // H2 콘솔 예외
                .headers(headers -> headers
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(
                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN
                        ))) // iframe 허용
                .formLogin(formLogin -> formLogin
                        .loginPage("/user/login") // 사용자 정의 로그인 페이지
                        .defaultSuccessUrl("/")) // 로그인 성공 후 리다이렉트 URL
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
                        .logoutSuccessUrl("/") // 로그아웃 성공 후 리다이렉트 URL
                        .invalidateHttpSession(true)) // 세션 무효화
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/user/login") // OAuth2 로그인 페이지
                        .defaultSuccessUrl("/") // 로그인 성공 후 리다이렉트 URL
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))); // CustomOAuth2UserService 등록

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호 암호화
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager(); // AuthenticationManager 빈 설정
    }
}