package org.example.sbb.comment;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.example.sbb.answer.Answer;
import org.example.sbb.question.Question;
import org.example.sbb.user.SiteUser;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    public Comment createQ(SiteUser siteUser, @NotEmpty(message = "내용은 필수항목입니다.") String content, Question question) {
        Comment comment = new Comment();
        comment.setCreateDate(LocalDateTime.now());
        comment.setAuthor(siteUser.getUsername());
        comment.setContent(content);
        comment.setQuestion(question);

        this.commentRepository.save(comment);
        return comment;
    }

    public Comment createA(SiteUser siteUser, @NotEmpty(message = "내용은 필수항목입니다.") String content, Answer answer) {
        Comment comment = new Comment();
        comment.setCreateDate(LocalDateTime.now());
        comment.setAuthor(siteUser.getUsername());
        comment.setContent(content);
        comment.setAnswer(answer);
        comment.setQuestion(answer.getQuestion());

        this.commentRepository.save(comment);
        return comment;
    }

    public List<Comment> findCommentsById(Integer id) {
        return this.commentRepository.findAllByQuestionId(id);
    }

    public List<Comment> findAll() {
        Sort sort = Sort.by(Sort.Direction.DESC, "createDate");
        List<Comment> comments = this.commentRepository.findAll(sort);
        return comments;
    }
}
