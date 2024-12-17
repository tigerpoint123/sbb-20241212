package org.example.sbb.answer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    List<Answer> findByAuthorId(int id);

//    @Query("select a.content, a.createDate, u.username from Answer a left outer join SiteUser u on a.author = u order by a.createDate desc")
//    List<Answer> findAllWithAuthor();
}
