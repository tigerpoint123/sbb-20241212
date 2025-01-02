package org.example.sbb.answer;

import lombok.RequiredArgsConstructor;
import org.example.sbb.DataNotFoundException;
import org.example.sbb.question.Question;
import org.example.sbb.user.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;

    public Answer create(Question question, String content, SiteUser author) {
        Answer answer = new Answer();
        answer.setContent(content);
        answer.setQuestion(question);
        answer.setCreateDate(LocalDateTime.now());
        answer.setAuthor(author);
        this.answerRepository.save(answer);
        return answer;
    }

    public Answer getAnswer(Integer id) {
        Optional<Answer> answer = this.answerRepository.findById(id);
        if (answer.isPresent()) return answer.get();
        else throw new DataNotFoundException("Answer not found");
    }

    public void modify(Answer answer, String content) {
        answer.setContent(content);
        answer.setModifyDate(LocalDateTime.now());
        this.answerRepository.save(answer);
    }

    public void deleteAnswer(Answer answer) {
        this.answerRepository.delete(answer);
    }

    public void vote(Answer answer, SiteUser siteUser) {
        answer.getVoter().add(siteUser);
        this.answerRepository.save(answer);
    }

    public List<Answer> findByAuthorId(int id) {
        return this.answerRepository.findByAuthorId(id);
    }

    public List<Answer> findAll() {
        Sort sort = Sort.by(Sort.Direction.DESC, "createDate");
        List<Answer> sorts = this.answerRepository.findAll(sort);
        return sorts;
    }

    public Page<Answer> findAnswerPaging(int id, int page, Pageable pageable, String sort) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        pageable = PageRequest.of(page, 3, Sort.by(sorts));
        switch (sort) {
            case "vote":
                return this.answerRepository.findByQuestionIdOrderByVoterDesc(id, pageable);
            default:
                return this.answerRepository.findAllByQuestion_Id(id, pageable);
        }
    }

}
