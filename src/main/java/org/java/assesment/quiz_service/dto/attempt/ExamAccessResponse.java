package org.java.assesment.quiz_service.dto.attempt;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Access-check result for an exam — mirrors ITBelts' ExamAccess constants.
 * Returned by GET /api/exams/{examId}/access
 *
 * accessStatus values:
 *   OK             — user may start a fresh attempt
 *   ALREADY_PASSED — user already passed; no retake allowed
 *   CAN_RETRY      — user failed at least once; free retry available
 */
@Data
@AllArgsConstructor
public class ExamAccessResponse {
    private Long   examId;
    private String accessStatus;   // OK | ALREADY_PASSED | CAN_RETRY
    private Integer bestScore;     // highest score across all attempts (null if none)
    private Integer bestPercentage;
    private int    attemptCount;
}
