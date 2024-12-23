package org.example.sbb.kakao;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class KakaoController {

    @GetMapping("/auth/kakao/callback")
    public String callback() {
        return "/question/list";
    }
}
