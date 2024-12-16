package org.example.sbb.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
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
            userService.create(userCreateForm.getUsername(), userCreateForm.getEmail(), userCreateForm.getPassword1());
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
        SiteUser siteUser = this.userService.getUser(principal.getName());

        //비밀번호 검증
        if (!userService.checkPassword(siteUser.getPassword(), currentPassword)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "modify_password";
        }
        userService.modifyPassword(siteUser, principal.getName(), siteUser.getEmail(), newPassword);
        return "redirect:/";
    }

}
