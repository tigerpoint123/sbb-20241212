package org.example.sbb.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sbb.answer.Answer;
import org.example.sbb.answer.AnswerService;
import org.example.sbb.question.Question;
import org.example.sbb.question.QuestionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final QuestionService questionService;
    private final AnswerService answerService;

    @GetMapping("/signup")
    public String signup( UserCreateForm userCreateForm) {
        return "signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return "signup_form";

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "Passwords do not match");
            return "signup_form";
        }

        try {
            userService.create(userCreateForm.getUsername(), userCreateForm.getEmail(), userCreateForm.getPassword1(), userCreateForm.getNickname());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signup_form";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup_form";
        }

        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "login_form";
    }

    @GetMapping("/findPassword")
    public String findPassword() {
        return "find_password";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestParam(value = "email") String email) {
        String newPassword = this.userService.sendTempPasswordEmail(email);
        SiteUser siteUser = this.userService.getUserFromEmail(email);

        this.userService.modifyPassword(siteUser, siteUser.getUsername(), siteUser.getEmail(), newPassword);
        return "redirect:/user/login";
    }

    @GetMapping("/modifyPassword")
    public String modifyPassword() {
        return "modify_password";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modifyPassword")
    public String modifyPassword(Principal principal,
                                 @RequestParam(value = "currentPassword") String currentPassword,
                                 @RequestParam(value = "newPassword") String newPassword, Model model) {
        SiteUser siteUser;
        if (principal instanceof OAuth2AuthenticationToken) {
            // OAuth2 (카카오 로그인) 사용자의 경우
            OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) principal;
            Map<String, Object> attributes = authToken.getPrincipal().getAttributes();
            String kakaoId = attributes.get("id").toString();
            siteUser = this.userService.getUserByKakaoId(kakaoId);
        } else {
            // 일반 로그인 사용자의 경우
            siteUser = this.userService.getUser(principal.getName());
        }

        //비밀번호 검증
        if (!userService.checkPassword(siteUser.getPassword(), currentPassword)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "modify_password";
        }
        userService.modifyPassword(siteUser, principal.getName(), siteUser.getEmail(), newPassword);
        return "redirect:/";
    }

    @PostMapping("/modifyInfo")
    public String modifyInfo(Principal principal,
                             @RequestParam(value = "username") String username,
                             @RequestParam(value = "email") String email,
                             @RequestParam(value = "password") String password) {
        SiteUser siteUser;
        if (principal instanceof OAuth2AuthenticationToken) {
            // OAuth2 (카카오 로그인) 사용자의 경우
            OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) principal;
            Map<String, Object> attributes = authToken.getPrincipal().getAttributes();
            String kakaoId = attributes.get("id").toString();
            siteUser = this.userService.getUserByKakaoId(kakaoId);
        } else {
            // 일반 로그인 사용자의 경우
            siteUser = this.userService.getUser(principal.getName());
        }

        userService.modifyInfo(siteUser, username, email, password);

        return "redirect:/";
    }

    @GetMapping("/myInfo")
    public String myInfo(Model model, Principal principal) {
        SiteUser siteUser;
        if (principal instanceof OAuth2AuthenticationToken) {
            // OAuth2 (카카오 로그인) 사용자의 경우
            OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) principal;
            Map<String, Object> attributes = authToken.getPrincipal().getAttributes();
            String kakaoId = attributes.get("id").toString();
            siteUser = this.userService.getUserByKakaoId(kakaoId);
        } else {
            // 일반 로그인 사용자의 경우
            siteUser = this.userService.getUser(principal.getName());
        }

        if (siteUser == null) {
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
        }

        List<Question> question = this.questionService.findByAuthorId((int) siteUser.getId());
        List<Answer> answers = this.answerService.findByAuthorId((int) siteUser.getId());
        model.addAttribute("nickname", siteUser.getNickname());
        model.addAttribute("email", siteUser.getEmail());
        model.addAttribute("questionList", question);
        model.addAttribute("answerList", answers);

        return "myInfo";
    }
}
