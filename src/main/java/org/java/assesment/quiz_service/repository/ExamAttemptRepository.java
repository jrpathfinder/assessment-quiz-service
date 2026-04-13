package org.java.assesment.quiz_service.repository;

import org.java.assesment.quiz_service.model.ExamAttempt;
import org.java.assesment.quiz_service.model.enums.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {

    /** All attempts for an exam, newest first — used for history display. */
    List<ExamAttempt> findByExamIdOrderByStartedAtDesc(Long examId);

    /** Check whether any attempt for this exam already has the given status. */
    boolean existsByExamIdAndStatus(Long examId, AttemptStatus status);
}
