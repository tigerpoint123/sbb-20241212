package org.example.sbb.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        Map<String, Object> attributes = oauth2User.getAttributes();

        // 카카오 사용자 정보 추출
        String id = attributes.get("id").toString();
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = kakaoAccount.get("email").toString();
        String username = ((Map<String, Object>) kakaoAccount.get("profile")).get("nickname").toString();

        // 사용자 저장 또는 업데이트
        SiteUser user = saveOrUpdateUser(id, email, username);

        // CustomOAuth2User 반환
        return new CustomOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                userNameAttributeName, // OAuth2User의 기본 키
                username,
                email,
                id
        );
    }

    private SiteUser saveOrUpdateUser(String kakaoId, String email, String username) {
        SiteUser user = userRepository.findByKakaoId(kakaoId)
                .orElse(SiteUser.builder()
                        .kakaoId(kakaoId)
                        .email(email)
                        .username(username)
                        .role(Role.USER)
                        .build());
        user.setNickname(username); // 닉네임 업데이트
        user.setUsername(username);
        return userRepository.save(user);
    }
}
