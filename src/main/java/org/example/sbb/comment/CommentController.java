package org.example.sbb.comment;

import lombok.RequiredArgsConstructor;
import org.example.sbb.answer.Answer;
import org.example.sbb.answer.AnswerService;
import org.example.sbb.question.Question;
import org.example.sbb.question.QuestionService;
import org.example.sbb.user.SiteUser;
import org.example.sbb.user.UserService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final UserService userService;
    private final QuestionService questionService;
    private final AnswerService answerService;

    @PostMapping("/questionCreate/{id}")
    public String createQusetion(@PathVariable int id, CommentForm commentForm, Principal principal) {
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

        Question question = this.questionService.getQuestion(id);
        Comment comment = this.commentService.createQ(siteUser, commentForm.getContent(), question);
        return "redirect:/question/detail/"+id;
    }

    @GetMapping("/recent")
    public String recent(Model model) {
        List<Comment> list = this.commentService.findAll();
        model.addAttribute("recentList", list);
        return "recent_comment";
    }

    @GetMapping("/answerComment/{id}")
    public String answerCreate(@PathVariable int id, Model model, CommentForm commentForm, Principal principal) {
        model.addAttribute("answerId", id);
        return "comment_form";
    }

    @PostMapping("/answerComment/{id}")
    public String answerComment(@PathVariable(value = "id") int id, CommentForm commentForm, Principal principal) {
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

        Answer answer = this.answerService.getAnswer(id);

        Comment comment = this.commentService.createA(siteUser, commentForm.getContent(), answer);
        return "redirect:/question/detail/"+answer.getQuestion().getId();
    }

}
