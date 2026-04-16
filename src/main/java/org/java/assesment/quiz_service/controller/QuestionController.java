package org.java.assesment.quiz_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java.assesment.quiz_service.dto.QuestionDTO;
import org.java.assesment.quiz_service.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public List<QuestionDTO> getAll(@RequestParam(required = false) Long examId) {
        if (examId != null) {
            return questionService.findByExam(examId);
        }
        return questionService.findAll();
    }

    @GetMapping("/{id}")
    public QuestionDTO getById(@PathVariable Long id) {
        return questionService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionDTO create(@Valid @RequestBody QuestionDTO dto) {
        return questionService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionDTO update(@PathVariable Long id, @Valid @RequestBody QuestionDTO dto) {
        return questionService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        questionService.delete(id);
    }
}
