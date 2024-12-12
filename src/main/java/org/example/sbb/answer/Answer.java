package org.example.sbb.answer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.sbb.question.Question;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    private LocalDateTime createDate;

//    @LastModifiedDate
//    private LocalDateTime modifyDate;

    @ManyToOne
    private Question question; // 얘가 부모 (many)
}
