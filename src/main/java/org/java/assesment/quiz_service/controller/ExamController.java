package org.java.assesment.quiz_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java.assesment.quiz_service.dto.ExamDTO;
import org.java.assesment.quiz_service.service.ExamService;
import org.springframework.http.HttpStatus;
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
    public ExamDTO create(@Valid @RequestBody ExamDTO dto) {
        return examService.create(dto);
    }

    @PutMapping("/{id}")
    public ExamDTO update(@PathVariable Long id, @Valid @RequestBody ExamDTO dto) {
        return examService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        examService.delete(id);
    }
}
