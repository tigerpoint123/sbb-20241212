package org.example.sbb.category;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.sbb.question.Question;

import java.util.List;

@Entity
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, name = "categoryName")
    private String name;

    @OneToMany
    private List<Question> question;

}
