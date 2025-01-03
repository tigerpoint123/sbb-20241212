package org.example.sbb;

import lombok.RequiredArgsConstructor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.example.sbb.user.SiteUser;
import org.example.sbb.user.UserService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CommonUtil {
    private final UserService userService;

    public String markdown(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    public SiteUser isKakaoUser(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            // OAuth2 (카카오 로그인) 사용자의 경우
            OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) principal;
            Map<String, Object> attributes = authToken.getPrincipal().getAttributes();
            String kakaoId = attributes.get("id").toString();
            return this.userService.getUserByKakaoId(kakaoId);
        } else {
            // 일반 로그인 사용자의 경우
            return this.userService.getUser(principal.getName());
        }
    }
}
