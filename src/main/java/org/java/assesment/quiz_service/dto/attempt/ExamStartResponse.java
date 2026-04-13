package org.java.assesment.quiz_service.dto.attempt;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ExamStartResponse {
    private Long   attemptId;
    private Long   examId;           // Added: lets frontend link back to exam
    private String examName;
    private String description;
    private Integer maxTimeMinutes;
    private Integer successPercentage;
    private LocalDateTime deadlineAt; // Added: server-authoritative deadline (UTC)
    private List<ExamQuestionDTO> questions;
}
