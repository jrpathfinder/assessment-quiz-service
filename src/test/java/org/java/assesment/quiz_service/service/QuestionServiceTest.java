package org.java.assesment.quiz_service.service;

import org.java.assesment.quiz_service.dto.PossibleAnswerDTO;
import org.java.assesment.quiz_service.dto.QuestionDTO;
import org.java.assesment.quiz_service.exception.ResourceNotFoundException;
import org.java.assesment.quiz_service.model.Category;
import org.java.assesment.quiz_service.model.Exam;
import org.java.assesment.quiz_service.model.PossibleAnswer;
import org.java.assesment.quiz_service.model.Question;
import org.java.assesment.quiz_service.model.enums.AnswerType;
import org.java.assesment.quiz_service.model.enums.ExamStatus;
import org.java.assesment.quiz_service.model.enums.QuestionStatus;
import org.java.assesment.quiz_service.repository.ExamRepository;
import org.java.assesment.quiz_service.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock private QuestionRepository questionRepository;
    @Mock private ExamRepository examRepository;

    @InjectMocks
    private QuestionService questionService;

    private Exam javaExam;
    private Question radioQuestion;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Java");
        category.setSlug("java");

        javaExam = new Exam();
        javaExam.setId(10L);
        javaExam.setName("Java Core");
        javaExam.setCategory(category);
        javaExam.setMaxTimeMinutes(60);
        javaExam.setSuccessPercentage(70);
        javaExam.setStatus(ExamStatus.RELEASED);
        javaExam.setCreatedAt(LocalDateTime.now());
        javaExam.setUpdatedAt(LocalDateTime.now());

        radioQuestion = new Question();
        radioQuestion.setId(100L);
        radioQuestion.setExam(javaExam);
        radioQuestion.setQuestionText("What is JVM?");
        radioQuestion.setExplanation("Java Virtual Machine");
        radioQuestion.setStatus(QuestionStatus.RELEASED);
        radioQuestion.setAnswerType(AnswerType.RADIO);
        radioQuestion.setCreatedAt(LocalDateTime.now());
        radioQuestion.setUpdatedAt(LocalDateTime.now());

        PossibleAnswer correct = new PossibleAnswer();
        correct.setId(1L);
        correct.setQuestion(radioQuestion);
        correct.setText("Java Virtual Machine");
        correct.setCorrect(true);
        correct.setOrderIndex(0);

        PossibleAnswer wrong = new PossibleAnswer();
        wrong.setId(2L);
        wrong.setQuestion(radioQuestion);
        wrong.setText("Java Variable Manager");
        wrong.setCorrect(false);
        wrong.setOrderIndex(1);

        radioQuestion.setPossibleAnswers(new ArrayList<>(List.of(correct, wrong)));
    }

    // ── findByExam ───────────────────────────────────────────────

    @Test
    void findByExam_returnsQuestionsForExam() {
        when(questionRepository.findByExamId(10L)).thenReturn(List.of(radioQuestion));

        List<QuestionDTO> result = questionService.findByExam(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuestionText()).isEqualTo("What is JVM?");
        assertThat(result.get(0).getExamId()).isEqualTo(10L);
    }

    @Test
    void findByExam_includesPossibleAnswers() {
        when(questionRepository.findByExamId(10L)).thenReturn(List.of(radioQuestion));

        QuestionDTO dto = questionService.findByExam(10L).get(0);

        assertThat(dto.getPossibleAnswers()).hasSize(2);
        assertThat(dto.getPossibleAnswers().get(0).isCorrect()).isTrue();
        assertThat(dto.getPossibleAnswers().get(1).isCorrect()).isFalse();
    }

    // ── findById ─────────────────────────────────────────────────

    @Test
    void findById_existingId_returnsDTO() {
        when(questionRepository.findById(100L)).thenReturn(Optional.of(radioQuestion));

        QuestionDTO dto = questionService.findById(100L);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getAnswerType()).isEqualTo("RADIO");
        assertThat(dto.getStatus()).isEqualTo("RELEASED");
        assertThat(dto.getExplanation()).isEqualTo("Java Virtual Machine");
    }

    @Test
    void findById_nonExistingId_throws() {
        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ── create ───────────────────────────────────────────────────

    @Test
    void create_validDTO_savesWithAnswers() {
        PossibleAnswerDTO a1 = new PossibleAnswerDTO();
        a1.setText("Correct answer");
        a1.setCorrect(true);
        a1.setOrderIndex(0);

        PossibleAnswerDTO a2 = new PossibleAnswerDTO();
        a2.setText("Wrong answer");
        a2.setCorrect(false);
        a2.setOrderIndex(1);

        QuestionDTO dto = new QuestionDTO();
        dto.setExamId(10L);
        dto.setQuestionText("What does OOP stand for?");
        dto.setAnswerType("RADIO");
        dto.setStatus("BETA");
        dto.setPossibleAnswers(List.of(a1, a2));

        Question saved = new Question();
        saved.setId(101L);
        saved.setExam(javaExam);
        saved.setQuestionText(dto.getQuestionText());
        saved.setStatus(QuestionStatus.BETA);
        saved.setAnswerType(AnswerType.RADIO);
        saved.setCreatedAt(LocalDateTime.now());
        saved.setUpdatedAt(LocalDateTime.now());

        PossibleAnswer pa = new PossibleAnswer();
        pa.setId(10L);
        pa.setText("Correct answer");
        pa.setCorrect(true);
        pa.setOrderIndex(0);
        pa.setQuestion(saved);
        saved.setPossibleAnswers(new ArrayList<>(List.of(pa)));

        when(examRepository.findById(10L)).thenReturn(Optional.of(javaExam));
        when(questionRepository.save(any(Question.class))).thenReturn(saved);

        QuestionDTO result = questionService.create(dto);

        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getPossibleAnswers()).hasSize(1);
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    void create_examNotFound_throws() {
        QuestionDTO dto = new QuestionDTO();
        dto.setExamId(99L);
        dto.setQuestionText("Orphan question");

        when(examRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.create(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── update ───────────────────────────────────────────────────

    @Test
    void update_replacesAnswers() {
        PossibleAnswerDTO newAnswer = new PossibleAnswerDTO();
        newAnswer.setText("Completely new answer");
        newAnswer.setCorrect(true);
        newAnswer.setOrderIndex(0);

        QuestionDTO updateDto = new QuestionDTO();
        updateDto.setExamId(10L);
        updateDto.setQuestionText("Updated question text");
        updateDto.setAnswerType("CHECKBOX");
        updateDto.setStatus("RELEASED");
        updateDto.setPossibleAnswers(List.of(newAnswer));

        Question updated = new Question();
        updated.setId(100L);
        updated.setExam(javaExam);
        updated.setQuestionText("Updated question text");
        updated.setStatus(QuestionStatus.RELEASED);
        updated.setAnswerType(AnswerType.CHECKBOX);
        updated.setCreatedAt(radioQuestion.getCreatedAt());
        updated.setUpdatedAt(LocalDateTime.now());
        PossibleAnswer pa = new PossibleAnswer();
        pa.setId(99L);
        pa.setText("Completely new answer");
        pa.setCorrect(true);
        pa.setOrderIndex(0);
        updated.setPossibleAnswers(new ArrayList<>(List.of(pa)));

        when(questionRepository.findById(100L)).thenReturn(Optional.of(radioQuestion));
        when(questionRepository.save(any(Question.class))).thenReturn(updated);

        QuestionDTO result = questionService.update(100L, updateDto);

        assertThat(result.getQuestionText()).isEqualTo("Updated question text");
        assertThat(result.getAnswerType()).isEqualTo("CHECKBOX");
        assertThat(result.getPossibleAnswers()).hasSize(1);
        assertThat(result.getPossibleAnswers().get(0).getText()).isEqualTo("Completely new answer");
    }

    @Test
    void update_nonExistingId_throws() {
        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.update(999L, new QuestionDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── delete ───────────────────────────────────────────────────

    @Test
    void delete_existingQuestion_deletes() {
        when(questionRepository.existsById(100L)).thenReturn(true);

        questionService.delete(100L);

        verify(questionRepository).deleteById(100L);
    }

    @Test
    void delete_nonExistingQuestion_throws() {
        when(questionRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> questionService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(questionRepository, never()).deleteById(any());
    }

    // ── findAll ──────────────────────────────────────────────────

    @Test
    void findAll_returnsMappedDTOs() {
        when(questionRepository.findAll()).thenReturn(List.of(radioQuestion));

        List<QuestionDTO> result = questionService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExamId()).isEqualTo(10L);
    }
}
