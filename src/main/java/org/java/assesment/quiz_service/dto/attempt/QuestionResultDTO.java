package org.java.assesment.quiz_service.dto.attempt;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuestionResultDTO {
    private Long questionId;
    private String questionText;
    private String explanation;
    private boolean correct;
    private List<AnswerResultDTO> answers;

    @Data
    @AllArgsConstructor
    public static class AnswerResultDTO {
        private Long id;
        private String text;
        private boolean isCorrect;
        private boolean wasSelected;
        private String explanation;  // why this option is right or wrong
    }
}
