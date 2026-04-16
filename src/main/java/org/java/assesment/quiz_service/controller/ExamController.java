package org.java.assesment.quiz_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java.assesment.quiz_service.dto.ExamDTO;
import org.java.assesment.quiz_service.service.ExamService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping
    public List<ExamDTO> getAll(@RequestParam(required = false) Long categoryId) {
        if (categoryId != null) {
            return examService.findByCategory(categoryId);
        }
        return examService.findAll();
    }

    @GetMapping("/{id}")
    public ExamDTO getById(@PathVariable Long id) {
        return examService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ExamDTO create(@Valid @RequestBody ExamDTO dto) {
        return examService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ExamDTO update(@PathVariable Long id, @Valid @RequestBody ExamDTO dto) {
        return examService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        examService.delete(id);
    }
}
