package org.example.sbb.answer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    List<Answer> findByAuthorId(int id);

    Page<Answer> findAllByQuestion_Id(Integer questionId, Pageable pageable);

    Page<Answer> findByQuestionIdOrderByVoterDesc(int questionId, Pageable pageable);
}
