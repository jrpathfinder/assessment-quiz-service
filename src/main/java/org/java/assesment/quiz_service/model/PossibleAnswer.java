package org.java.assesment.quiz_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One answer option for a Question.
 * A question can have multiple PossibleAnswers; one or more may be correct
 * depending on the question's AnswerType.
 */
@Entity
@Table(name = "possible_answer")
@Data
@NoArgsConstructor
public class PossibleAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    /** Why this option is correct or incorrect — shown in the result review (like ITBelts). */
    @Column(columnDefinition = "TEXT")
    private String explanation;
}
