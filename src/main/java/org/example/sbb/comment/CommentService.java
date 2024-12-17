package org.example.sbb.comment;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.example.sbb.answer.Answer;
import org.example.sbb.question.Question;
import org.example.sbb.user.SiteUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

        this.commentRepository.save(comment);
        return comment;
    }
}
