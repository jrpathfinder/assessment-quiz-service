package org.java.assesment.quiz_service.dto.attempt;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** Question sent to client during an active attempt — no correct-answer hints */
@Data
@AllArgsConstructor
public class ExamQuestionDTO {
    private Long id;
    private String questionText;
    private String answerType;          // RADIO | CHECKBOX
    private List<AnswerOptionDTO> options;
}
