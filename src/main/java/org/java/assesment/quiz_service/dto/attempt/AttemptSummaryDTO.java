package org.java.assesment.quiz_service.dto.attempt;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * One row in a user's attempt history for a given exam.
 * Returned by GET /api/exams/{examId}/attempts
 */
@Data
@AllArgsConstructor
public class AttemptSummaryDTO {
    private Long   attemptId;
    private String status;          // IN_PROGRESS | PASSED | FAILED_LOW_SCORE | FAILED_TIMEOUT
    private Integer score;
    private Integer total;
    private Integer percentage;
    private Boolean passed;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
}
