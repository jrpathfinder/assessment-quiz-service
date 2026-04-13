package org.java.assesment.quiz_service.dto.attempt;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Answer option sent to client — NO isCorrect field */
@Data
@AllArgsConstructor
public class AnswerOptionDTO {
    private Long id;
    private String text;
    private Integer orderIndex;
}
