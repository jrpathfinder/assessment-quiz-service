package org.java.assesment.quiz_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class QuestionDTO {

    private Long id;

    @NotNull
    private Long examId;

    @NotBlank
    private String questionText;

    private String explanation;

    private String status;      // QuestionStatus enum name

    private String answerType;  // AnswerType enum name

    private List<PossibleAnswerDTO> possibleAnswers = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
