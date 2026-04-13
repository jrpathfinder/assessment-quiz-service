package org.java.assesment.quiz_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PossibleAnswerDTO {

    private Long id;

    @NotBlank
    private String text;

    private boolean correct;

    private Integer orderIndex = 0;
}
