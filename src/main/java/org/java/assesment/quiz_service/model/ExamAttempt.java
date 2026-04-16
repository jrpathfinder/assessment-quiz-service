package org.java.assesment.quiz_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.java.assesment.quiz_service.model.enums.AttemptStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exam_attempt")
@Data
@NoArgsConstructor
public class ExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    /** When the attempt was created (exam clock starts). Stored in UTC. */
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    /** Server-calculated deadline = startedAt + exam.maxTimeMinutes. */
    @Column(name = "deadline_at", nullable = false)
    private LocalDateTime deadlineAt;

    /** When the user actually submitted (may be after deadlineAt → FAILED_TIMEOUT). */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    private Integer score;
    private Integer total;
    private Integer percentage;
    private Boolean passed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    /** The user who started this attempt. Null for legacy/anonymous attempts. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttemptAnswer> answers = new ArrayList<>();
}
