package org.java.assesment.quiz_service.service;

import lombok.RequiredArgsConstructor;
import org.java.assesment.quiz_service.dto.attempt.*;
import org.java.assesment.quiz_service.exception.ResourceNotFoundException;
import org.java.assesment.quiz_service.model.*;
import org.java.assesment.quiz_service.model.enums.AttemptStatus;
import org.java.assesment.quiz_service.repository.AppUserRepository;
import org.java.assesment.quiz_service.repository.ExamAttemptRepository;
import org.java.assesment.quiz_service.repository.ExamRepository;
import org.java.assesment.quiz_service.repository.QuestionRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamAttemptService {

    private final ExamRepository        examRepository;
    private final QuestionRepository    questionRepository;
    private final ExamAttemptRepository attemptRepository;
    private final AppUserRepository     userRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Start exam
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new attempt, records the server-authoritative deadline,
     * and returns questions in RANDOMISED order WITHOUT correct-answer flags.
     */
    @Transactional
    public ExamStartResponse startExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam", examId));

        List<Question> questions = questionRepository.findByExamId(examId);

        // ── Randomise question order (ITBelts pattern) ──────────────────────
        List<Question> shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled);

        // ── Create attempt with server-calculated deadline ───────────────────
        LocalDateTime now      = LocalDateTime.now();
        LocalDateTime deadline = now.plusMinutes(exam.getMaxTimeMinutes());

        // ── Link attempt to authenticated user (if any) ─────────────────────
        AppUser currentUser = getCurrentUser();

        ExamAttempt attempt = new ExamAttempt();
        attempt.setExam(exam);
        attempt.setUser(currentUser);
        attempt.setStartedAt(now);
        attempt.setDeadlineAt(deadline);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt = attemptRepository.save(attempt);

        // ── Build question DTOs — NO isCorrect flag exposed ──────────────────
        List<ExamQuestionDTO> questionDTOs = shuffled.stream()
                .map(q -> new ExamQuestionDTO(
                        q.getId(),
                        q.getQuestionText(),
                        q.getAnswerType().name(),
                        q.getPossibleAnswers().stream()
                                .map(a -> new AnswerOptionDTO(a.getId(), a.getText(), a.getOrderIndex()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        return new ExamStartResponse(
                attempt.getId(),
                exam.getId(),
                exam.getName(),
                exam.getDescription(),
                exam.getMaxTimeMinutes(),
                exam.getSuccessPercentage(),
                deadline,
                questionDTOs
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Submit attempt
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Scores the attempt server-side.
     * If submitted after deadlineAt → FAILED_TIMEOUT (no scoring, ITBelts pattern).
     * Scoring is all-or-nothing per question: checkbox must match ALL correct options.
     */
    @Transactional
    public ExamResultResponse submitAttempt(Long attemptId, SubmitAnswerRequest request) {
        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", attemptId));

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Attempt already submitted");
        }

        LocalDateTime submittedAt = LocalDateTime.now();
        attempt.setSubmittedAt(submittedAt);

        // ── ITBelts: check deadline server-side ──────────────────────────────
        if (submittedAt.isAfter(attempt.getDeadlineAt())) {
            attempt.setStatus(AttemptStatus.FAILED_TIMEOUT);
            attempt.setScore(0);
            attempt.setTotal(questionRepository.findByExamId(attempt.getExam().getId()).size());
            attempt.setPercentage(0);
            attempt.setPassed(false);
            attemptRepository.save(attempt);

            return new ExamResultResponse(
                    attemptId,
                    attempt.getExam().getId(),
                    AttemptStatus.FAILED_TIMEOUT.name(),
                    0, attempt.getTotal(), 0, false,
                    attempt.getExam().getSuccessPercentage(),
                    Collections.emptyList()   // no review on timeout
            );
        }

        // ── Load questions & build submission lookup ─────────────────────────
        List<Question> questions = questionRepository.findByExamId(attempt.getExam().getId());
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        Map<Long, Set<Long>> submittedAnswers = new HashMap<>();
        if (request.getAnswers() != null) {
            for (SubmitAnswerRequest.QuestionAnswer qa : request.getAnswers()) {
                submittedAnswers.put(
                        qa.getQuestionId(),
                        qa.getSelectedAnswerIds() == null ? Set.of() : new HashSet<>(qa.getSelectedAnswerIds())
                );
            }
        }

        // ── Score each question (all-or-nothing per ITBelts) ─────────────────
        int score = 0;
        List<QuestionResultDTO> questionResults = new ArrayList<>();

        for (Question q : questions) {
            Set<Long> correctIds = q.getPossibleAnswers().stream()
                    .filter(PossibleAnswer::isCorrect)
                    .map(PossibleAnswer::getId)
                    .collect(Collectors.toSet());

            Set<Long> selected = submittedAnswers.getOrDefault(q.getId(), Set.of());

            // All-or-nothing: selected set must exactly equal correct set
            boolean isCorrect = correctIds.equals(selected);
            if (isCorrect) score++;

            // Persist per-question answer record
            AttemptAnswer aa = new AttemptAnswer();
            aa.setAttempt(attempt);
            aa.setQuestion(q);
            aa.setSelectedIds(selected.stream().map(String::valueOf).collect(Collectors.joining(",")));
            attempt.getAnswers().add(aa);

            // Build per-answer result with correctness revealed (safe — this is POST-submit)
            List<QuestionResultDTO.AnswerResultDTO> answerResults = q.getPossibleAnswers().stream()
                    .map(a -> new QuestionResultDTO.AnswerResultDTO(
                            a.getId(), a.getText(),
                            a.isCorrect(),
                            selected.contains(a.getId()),
                            a.getExplanation()
                    ))
                    .collect(Collectors.toList());

            questionResults.add(new QuestionResultDTO(
                    q.getId(), q.getQuestionText(), q.getExplanation(),
                    isCorrect, answerResults
            ));
        }

        int total      = questions.size();
        int percentage = total > 0 ? (score * 100 / total) : 0;
        boolean passed = percentage >= attempt.getExam().getSuccessPercentage();
        AttemptStatus finalStatus = passed ? AttemptStatus.PASSED : AttemptStatus.FAILED_LOW_SCORE;

        attempt.setScore(score);
        attempt.setTotal(total);
        attempt.setPercentage(percentage);
        attempt.setPassed(passed);
        attempt.setStatus(finalStatus);
        attemptRepository.save(attempt);

        return new ExamResultResponse(
                attemptId,
                attempt.getExam().getId(),
                finalStatus.name(),
                score, total, percentage, passed,
                attempt.getExam().getSuccessPercentage(),
                questionResults
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Attempt history
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns all completed attempts for an exam, newest first.
     * Mirrors ITBelts' UserExam history list shown on the exam detail page.
     */
    @Transactional(readOnly = true)
    public List<AttemptSummaryDTO> getAttemptHistory(Long examId) {
        return attemptRepository.findByExamIdOrderByStartedAtDesc(examId).stream()
                .map(a -> new AttemptSummaryDTO(
                        a.getId(),
                        a.getStatus().name(),
                        a.getScore(),
                        a.getTotal(),
                        a.getPercentage(),
                        a.getPassed(),
                        a.getStartedAt(),
                        a.getSubmittedAt()
                ))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Access check
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Checks whether the user may start a new exam attempt.
     * Rules (from ITBelts ExamAccess):
     *   • ALREADY_PASSED  → at least one PASSED attempt exists
     *   • CAN_RETRY       → has failed attempt(s) but no pass
     *   • OK              → no attempts yet
     */
    @Transactional(readOnly = true)
    public ExamAccessResponse getExamAccess(Long examId) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Exam", examId);
        }

        List<ExamAttempt> attempts = attemptRepository.findByExamIdOrderByStartedAtDesc(examId);

        boolean hasPassed = attempts.stream()
                .anyMatch(a -> a.getStatus() == AttemptStatus.PASSED);

        boolean hasFailed = attempts.stream()
                .anyMatch(a -> a.getStatus() == AttemptStatus.FAILED_LOW_SCORE
                            || a.getStatus() == AttemptStatus.FAILED_TIMEOUT);

        // Best score across all attempts
        Integer bestScore = attempts.stream()
                .filter(a -> a.getScore() != null)
                .mapToInt(ExamAttempt::getScore)
                .max()
                .isPresent()
                ? attempts.stream().filter(a -> a.getScore() != null).mapToInt(ExamAttempt::getScore).max().getAsInt()
                : null;

        Integer bestPercentage = attempts.stream()
                .filter(a -> a.getPercentage() != null)
                .mapToInt(ExamAttempt::getPercentage)
                .max()
                .isPresent()
                ? attempts.stream().filter(a -> a.getPercentage() != null).mapToInt(ExamAttempt::getPercentage).max().getAsInt()
                : null;

        // This is a study tool — retaking is always allowed.
        // ALREADY_PASSED is informational only (shows best score banner), not a block.
        String accessStatus;
        if (hasPassed) {
            accessStatus = "ALREADY_PASSED";   // shows congrats banner, but Start is still enabled
        } else if (hasFailed) {
            accessStatus = "CAN_RETRY";
        } else {
            accessStatus = "OK";
        }

        return new ExamAccessResponse(examId, accessStatus, bestScore, bestPercentage, attempts.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper: resolve current user from JWT (null = anonymous)
    // ─────────────────────────────────────────────────────────────────────────

    private AppUser getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            return null;
        }
        try {
            Long userId = (Long) auth.getPrincipal();
            return userRepository.findById(userId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
