package org.java.assesment.quiz_service.controller;

import lombok.RequiredArgsConstructor;
import org.java.assesment.quiz_service.dto.attempt.*;
import org.java.assesment.quiz_service.service.ExamAttemptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExamAttemptController {

    private final ExamAttemptService attemptService;

    /**
     * POST /api/exams/{examId}/start
     * Creates a new attempt. Returns shuffled questions WITHOUT correct-answer flags.
     * Also returns deadlineAt so the frontend can display an authoritative countdown.
     */
    @PostMapping("/exams/{examId}/start")
    public ResponseEntity<ExamStartResponse> start(@PathVariable Long examId) {
        return ResponseEntity.ok(attemptService.startExam(examId));
    }

    /**
     * POST /api/attempts/{attemptId}/submit
     * Scores the attempt server-side.
     * Returns PASSED | FAILED_LOW_SCORE | FAILED_TIMEOUT with full question breakdown.
     */
    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<ExamResultResponse> submit(
            @PathVariable Long attemptId,
            @RequestBody SubmitAnswerRequest request) {
        return ResponseEntity.ok(attemptService.submitAttempt(attemptId, request));
    }

    /**
     * GET /api/exams/{examId}/attempts
     * Returns all past attempts for this exam, newest first.
     * Used to display attempt history on the exam detail / result screen.
     */
    @GetMapping("/exams/{examId}/attempts")
    public ResponseEntity<List<AttemptSummaryDTO>> history(@PathVariable Long examId) {
        return ResponseEntity.ok(attemptService.getAttemptHistory(examId));
    }

    /**
     * GET /api/exams/{examId}/access
     * Returns OK | ALREADY_PASSED | CAN_RETRY so the frontend can
     * show the correct action button without leaking answers.
     */
    @GetMapping("/exams/{examId}/access")
    public ResponseEntity<ExamAccessResponse> access(@PathVariable Long examId) {
        return ResponseEntity.ok(attemptService.getExamAccess(examId));
    }
}
