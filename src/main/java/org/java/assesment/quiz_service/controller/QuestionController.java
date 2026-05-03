package org.java.assesment.quiz_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java.assesment.quiz_service.dto.QuestionDTO;
import org.java.assesment.quiz_service.dto.question.AiGenerateRequest;
import org.java.assesment.quiz_service.dto.question.BatchImportRequest;
import org.java.assesment.quiz_service.service.AiQuestionService;
import org.java.assesment.quiz_service.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final AiQuestionService aiQuestionService;

    @GetMapping
    public List<QuestionDTO> getAll(@RequestParam(required = false) Long examId) {
        if (examId != null) return questionService.findByExam(examId);
        return questionService.findAll();
    }

    @GetMapping("/{id}")
    public QuestionDTO getById(@PathVariable Long id) {
        return questionService.findById(id);
    }

    // ── Single create ────────────────────────────────────────────────────────

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionDTO create(@Valid @RequestBody QuestionDTO dto) {
        return questionService.create(dto);
    }

    // ── Batch JSON import ────────────────────────────────────────────────────

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> batchImport(@Valid @RequestBody BatchImportRequest request) {
        List<QuestionDTO> created = questionService.batchImport(request);
        return Map.of(
                "imported", created.size(),
                "status", "BETA",
                "questions", created
        );
    }

    // ── AI generation ────────────────────────────────────────────────────────

    @PostMapping("/ai-generate")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> aiGenerate(@Valid @RequestBody AiGenerateRequest request) {
        List<BatchImportRequest.QuestionImportItem> items =
                aiQuestionService.generateQuestions(request.topic(), request.javaVersion(), request.count());

        BatchImportRequest batchRequest = new BatchImportRequest(request.examId(), items);
        List<QuestionDTO> created = questionService.batchImport(batchRequest);

        return Map.of(
                "generated", created.size(),
                "topic", request.topic(),
                "javaVersion", request.javaVersion(),
                "status", "BETA",
                "questions", created
        );
    }

    // ── Update / Delete ──────────────────────────────────────────────────────

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
