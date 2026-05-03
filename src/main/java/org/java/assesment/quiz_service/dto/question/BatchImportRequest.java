package org.java.assesment.quiz_service.dto.question;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BatchImportRequest(
        @NotNull Long examId,
        @NotEmpty List<QuestionImportItem> questions
) {
    public record QuestionImportItem(
            @NotNull String questionText,
            String explanation,
            String answerType,   // RADIO | CHECKBOX  (default RADIO)
            String status,       // BETA | RELEASED   (default BETA)
            @NotEmpty List<AnswerImportItem> answers
    ) {}

    public record AnswerImportItem(
            @NotNull String text,
            boolean correct,
            String explanation,
            int orderIndex
    ) {}
}
