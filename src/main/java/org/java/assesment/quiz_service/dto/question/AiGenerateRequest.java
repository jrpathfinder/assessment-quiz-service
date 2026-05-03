package org.java.assesment.quiz_service.dto.question;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AiGenerateRequest(
        @NotNull  Long   examId,
        @NotBlank String topic,
        @NotBlank String javaVersion,
        @Min(1) @Max(30) int count
) {}
