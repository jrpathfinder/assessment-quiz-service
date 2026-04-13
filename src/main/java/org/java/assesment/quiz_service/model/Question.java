package org.java.assesment.quiz_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.java.assesment.quiz_service.model.enums.AnswerType;
import org.java.assesment.quiz_service.model.enums.QuestionStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A quiz question belonging to an Exam.
 * Contains one or more PossibleAnswers (at least one marked correct).
 */
@Entity
@Table(name = "question")
@Data
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(columnDefinition = "TEXT")
    private String explanation;     // shown after answering

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionStatus status = QuestionStatus.BETA;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_type", nullable = false, length = 20)
    private AnswerType answerType = AnswerType.RADIO;

    @OneToMany(
        mappedBy = "question",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @OrderBy("orderIndex ASC")
    private List<PossibleAnswer> possibleAnswers = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
