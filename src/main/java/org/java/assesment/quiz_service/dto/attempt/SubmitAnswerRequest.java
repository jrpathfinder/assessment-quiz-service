package org.java.assesment.quiz_service.dto.attempt;

import lombok.Data;

import java.util.List;

@Data
public class SubmitAnswerRequest {
    /** Each entry maps one question to the IDs the user selected */
    private List<QuestionAnswer> answers;

    @Data
    public static class QuestionAnswer {
        private Long questionId;
        private List<Long> selectedAnswerIds;
    }
}
