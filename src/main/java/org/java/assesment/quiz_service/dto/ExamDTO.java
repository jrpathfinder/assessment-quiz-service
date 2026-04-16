package org.java.assesment.quiz_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamDTO {

    private Long id;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Long categoryId;

    private String categoryName;  // read-only, populated on response

    @Min(1) @Max(480)
    private Integer maxTimeMinutes = 60;

    @Min(1) @Max(100)
    private Integer successPercentage = 70;

    private String status;  // ExamStatus enum name

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
