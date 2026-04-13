package org.java.assesment.quiz_service.model.enums;

/**
 * Lifecycle states of an exam attempt — mirrors ITBelts' result constants:
 *   ONGOING=0, PASSED=1, FAILED_OUT_OF_TIME=2, FAILED_LOW_SCORE=3
 */
public enum AttemptStatus {
    IN_PROGRESS,      // Exam started, not yet submitted
    PASSED,           // Submitted in time, score >= successPercentage
    FAILED_LOW_SCORE, // Submitted in time, score < successPercentage
    FAILED_TIMEOUT    // Submitted after the deadline — no scoring applied
}
