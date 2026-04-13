package org.java.assesment.quiz_service.dto.attempt;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ExamResultResponse {
    private Long   attemptId;
    private Long   examId;           // Added: lets frontend link back to exam
    private String status;           // PASSED | FAILED_LOW_SCORE | FAILED_TIMEOUT
    private int    score;
    private int    total;
    private int    percentage;
    private boolean passed;
    private int    requiredPercentage;
    private List<QuestionResultDTO> questions; // empty on FAILED_TIMEOUT
}
