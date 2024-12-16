package org.example.sbb.category;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.sbb.question.Question;

@Entity
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    private Question question;


}
