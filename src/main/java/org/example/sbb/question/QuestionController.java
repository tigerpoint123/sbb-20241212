package org.example.sbb.question;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sbb.answer.Answer;
import org.example.sbb.answer.AnswerForm;
import org.example.sbb.answer.AnswerService;
import org.example.sbb.category.Category;
import org.example.sbb.category.CategoryForm;
import org.example.sbb.category.CategoryService;
import org.example.sbb.comment.Comment;
import org.example.sbb.comment.CommentForm;
import org.example.sbb.comment.CommentService;
import org.example.sbb.user.SiteUser;
import org.example.sbb.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/question")
public class QuestionController {
    private final QuestionService questionService;
    private final UserService userService;
    private final CommentService commentService;
    private final AnswerService answerService;
    private final CategoryService categoryService;

    @GetMapping("/list")
    public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "kw", defaultValue = "") String kw) {
        Page<Question> paging = this.questionService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);

        return "question_list";
    }

    @GetMapping(value = "/detail/{id}/{sort}")
    public String detail(Model model, @PathVariable(value = "id") Integer id, @RequestParam(value = "page", defaultValue = "0") int page,
                         AnswerForm answerForm, CommentForm commentForm, Pageable pageable,
                         @PathVariable(value = "sort") String sort) {
        Question question = this.questionService.getQuestion(id);
        List<Comment> comments = commentService.findCommentsById(id);
        Page<Answer> answerPage = this.answerService.findAnswerPaging(id, page, pageable, sort);

        model.addAttribute("question", question);
        model.addAttribute("comments", comments);
        model.addAttribute("paging", answerPage);
        return "question_detail";
    }

    @GetMapping(value = "/detail/{id}")
    public String detail(Model model, @PathVariable(value = "id") Integer id, @RequestParam(value = "page", defaultValue = "0") int page,
                         AnswerForm answerForm, CommentForm commentForm, Pageable pageable, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        Question question = this.questionService.getQuestion(id);

        // 조회수 용 쿠키 코드
        String sessionId = session.getId();
        Cookie cookie = new Cookie("question_" + id + "_" + sessionId, "");
        cookie.setMaxAge(60 * 60 * 2); // 24시간 유효
        Cookie[] cookies = request.getCookies();
        boolean isExist = false;
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals(cookie.getName())) {
                    isExist = true;
                    break;
                }
            }
        }
        if (!isExist) {
            this.questionService.countView(question.getViews(), question);
            response.addCookie(cookie);
        }
        // 쿠키 코드 끝

        List<Comment> comments = commentService.findCommentsById(id);
        String sort = "createDate";
        Page<Answer> answerPage = this.answerService.findAnswerPaging(id, page, pageable, sort);

        model.addAttribute("question", question);
        model.addAttribute("comments", comments);
        model.addAttribute("paging", answerPage);
        return "question_detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String questionCreate(QuestionForm questionForm, Model model, CategoryForm categoryForm) {
        List<Category> category = categoryService.findName();
        model.addAttribute("questionForm", questionForm);
        model.addAttribute("categories", category);
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) return "question_form";

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
        Category category = categoryService.findCategory(Long.valueOf(questionForm.getCategory().getId()));
        questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser, category);
        return "redirect:/question/list";
    }

    @GetMapping("/modify/{id}")
    public String modify(@PathVariable("id") Integer id, QuestionForm questionForm, Principal principal) {
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");

        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        return "question_form";
    }

    @PostMapping("/modify/{id}")
    @PreAuthorize("isAuthenticated()")
    public String modify(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal, @PathVariable("id") Integer id) {
        if (bindingResult.hasErrors()) return "question_form";
        Question question = this.questionService.getQuestion(id);

        if (!question.getAuthor().getUsername().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다");
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
        return "redirect:/question/detail/" + id;
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(@PathVariable("id") Integer id, Principal principal) {
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다");

        this.questionService.delete(question);
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String vote(@PathVariable("id") Integer id, Principal principal) {
        Question question = this.questionService.getQuestion(id);

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

        this.questionService.vote(question, siteUser);
        return "redirect:/question/detail/" + id;
    }

}
