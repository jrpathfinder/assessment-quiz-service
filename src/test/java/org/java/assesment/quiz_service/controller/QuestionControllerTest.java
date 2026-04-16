package org.java.assesment.quiz_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java.assesment.quiz_service.dto.PossibleAnswerDTO;
import org.java.assesment.quiz_service.dto.QuestionDTO;
import org.java.assesment.quiz_service.exception.ResourceNotFoundException;
import org.java.assesment.quiz_service.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class QuestionControllerTest {

    @Mock  private QuestionService questionService;
    @InjectMocks private QuestionController questionController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    private QuestionDTO questionDto;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(questionController)
                .setValidator(validator)
                .build();

        PossibleAnswerDTO correct = new PossibleAnswerDTO();
        correct.setId(1L);
        correct.setText("Java Virtual Machine");
        correct.setCorrect(true);
        correct.setOrderIndex(0);

        PossibleAnswerDTO wrong = new PossibleAnswerDTO();
        wrong.setId(2L);
        wrong.setText("Java Variable Manager");
        wrong.setCorrect(false);
        wrong.setOrderIndex(1);

        questionDto = new QuestionDTO();
        questionDto.setId(100L);
        questionDto.setExamId(10L);
        questionDto.setQuestionText("What is JVM?");
        questionDto.setExplanation("Java Virtual Machine");
        questionDto.setStatus("RELEASED");
        questionDto.setAnswerType("RADIO");
        questionDto.setPossibleAnswers(List.of(correct, wrong));
        questionDto.setCreatedAt(LocalDateTime.now());
        questionDto.setUpdatedAt(LocalDateTime.now());
    }

    // ── GET /api/questions ───────────────────────────────────────

    @Test
    void getAll_noFilter_returnsAll() throws Exception {
        when(questionService.findAll()).thenReturn(List.of(questionDto));

        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].questionText").value("What is JVM?"));

        verify(questionService).findAll();
    }

    @Test
    void getAll_withExamFilter_returnsByExam() throws Exception {
        when(questionService.findByExam(10L)).thenReturn(List.of(questionDto));

        mockMvc.perform(get("/api/questions?examId=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].examId").value(10));

        verify(questionService).findByExam(10L);
        verify(questionService, never()).findAll();
    }

    // ── GET /api/questions/{id} ──────────────────────────────────

    @Test
    void getById_existingId_returns200WithAnswers() throws Exception {
        when(questionService.findById(100L)).thenReturn(questionDto);

        mockMvc.perform(get("/api/questions/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.answerType").value("RADIO"))
                .andExpect(jsonPath("$.possibleAnswers.length()").value(2))
                .andExpect(jsonPath("$.possibleAnswers[0].correct").value(true))
                .andExpect(jsonPath("$.possibleAnswers[1].correct").value(false));
    }

    @Test
    void getById_nonExistingId_returns404() throws Exception {
        when(questionService.findById(999L)).thenThrow(new ResourceNotFoundException("Question", 999L));

        mockMvc.perform(get("/api/questions/999"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/questions ──────────────────────────────────────

    @Test
    void create_validBody_returns201() throws Exception {
        PossibleAnswerDTO a = new PossibleAnswerDTO();
        a.setText("Correct");
        a.setCorrect(true);
        a.setOrderIndex(0);

        QuestionDTO request = new QuestionDTO();
        request.setExamId(10L);
        request.setQuestionText("What does JVM stand for?");
        request.setAnswerType("RADIO");
        request.setPossibleAnswers(List.of(a));

        when(questionService.create(any(QuestionDTO.class))).thenReturn(questionDto);

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void create_missingQuestionText_returns400() throws Exception {
        QuestionDTO request = new QuestionDTO();
        request.setExamId(10L);

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_missingExamId_returns400() throws Exception {
        QuestionDTO request = new QuestionDTO();
        request.setQuestionText("Question with no exam");

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/questions/{id} ──────────────────────────────────

    @Test
    void update_existingId_returns200() throws Exception {
        PossibleAnswerDTO a = new PossibleAnswerDTO();
        a.setText("Updated answer");
        a.setCorrect(true);
        a.setOrderIndex(0);

        QuestionDTO request = new QuestionDTO();
        request.setExamId(10L);
        request.setQuestionText("Updated question?");
        request.setAnswerType("CHECKBOX");
        request.setPossibleAnswers(List.of(a));

        QuestionDTO updated = new QuestionDTO();
        updated.setId(100L);
        updated.setQuestionText("Updated question?");
        updated.setAnswerType("CHECKBOX");
        updated.setStatus("RELEASED");
        updated.setPossibleAnswers(List.of(a));

        when(questionService.update(eq(100L), any(QuestionDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/questions/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionText").value("Updated question?"))
                .andExpect(jsonPath("$.answerType").value("CHECKBOX"));
    }

    @Test
    void update_nonExistingId_returns404() throws Exception {
        QuestionDTO request = new QuestionDTO();
        request.setExamId(10L);
        request.setQuestionText("Ghost question");

        when(questionService.update(eq(999L), any(QuestionDTO.class)))
                .thenThrow(new ResourceNotFoundException("Question", 999L));

        mockMvc.perform(put("/api/questions/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/questions/{id} ───────────────────────────────

    @Test
    void delete_existingId_returns204() throws Exception {
        doNothing().when(questionService).delete(100L);

        mockMvc.perform(delete("/api/questions/100"))
                .andExpect(status().isNoContent());

        verify(questionService).delete(100L);
    }

    @Test
    void delete_nonExistingId_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Question", 999L)).when(questionService).delete(999L);

        mockMvc.perform(delete("/api/questions/999"))
                .andExpect(status().isNotFound());
    }
}
