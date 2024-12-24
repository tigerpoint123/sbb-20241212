package org.example.sbb.user;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    private final String username;
    private final String email;
    private final String kakaoId;

    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes,
                            String nameAttributeKey,
                            String username,
                            String email,
                            String kakaoId) {
        super(authorities, attributes, nameAttributeKey);
        this.username = username;
        this.email = email;
        this.kakaoId = kakaoId;
    }
}
