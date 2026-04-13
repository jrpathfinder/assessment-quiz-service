package org.java.assesment.quiz_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java.assesment.quiz_service.dto.ExamDTO;
import org.java.assesment.quiz_service.exception.ResourceNotFoundException;
import org.java.assesment.quiz_service.service.ExamService;
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
class ExamControllerTest {

    @Mock  private ExamService examService;
    @InjectMocks private ExamController examController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    private ExamDTO javaExamDto;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(examController)
                .setValidator(validator)
                .build();

        javaExamDto = new ExamDTO();
        javaExamDto.setId(10L);
        javaExamDto.setName("Java Core – Junior");
        javaExamDto.setCategoryId(1L);
        javaExamDto.setCategoryName("Java");
        javaExamDto.setMaxTimeMinutes(60);
        javaExamDto.setSuccessPercentage(70);
        javaExamDto.setStatus("DRAFT");
        javaExamDto.setCreatedAt(LocalDateTime.now());
        javaExamDto.setUpdatedAt(LocalDateTime.now());
    }

    // ── GET /api/exams ───────────────────────────────────────────

    @Test
    void getAll_noCategoryFilter_returnsAllExams() throws Exception {
        when(examService.findAll()).thenReturn(List.of(javaExamDto));

        mockMvc.perform(get("/api/exams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Java Core – Junior"))
                .andExpect(jsonPath("$[0].categoryName").value("Java"));
    }

    @Test
    void getAll_withCategoryFilter_returnsFilteredExams() throws Exception {
        when(examService.findByCategory(1L)).thenReturn(List.of(javaExamDto));

        mockMvc.perform(get("/api/exams?categoryId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoryId").value(1));

        verify(examService).findByCategory(1L);
        verify(examService, never()).findAll();
    }

    // ── GET /api/exams/{id} ──────────────────────────────────────

    @Test
    void getById_existingId_returns200() throws Exception {
        when(examService.findById(10L)).thenReturn(javaExamDto);

        mockMvc.perform(get("/api/exams/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.maxTimeMinutes").value(60));
    }

    @Test
    void getById_nonExistingId_returns404() throws Exception {
        when(examService.findById(99L)).thenThrow(new ResourceNotFoundException("Exam", 99L));

        mockMvc.perform(get("/api/exams/99"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/exams ──────────────────────────────────────────

    @Test
    void create_validBody_returns201() throws Exception {
        ExamDTO request = new ExamDTO();
        request.setCategoryId(1L);
        request.setName("New Exam");
        request.setMaxTimeMinutes(45);
        request.setSuccessPercentage(65);

        when(examService.create(any(ExamDTO.class))).thenReturn(javaExamDto);

        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void create_missingName_returns400() throws Exception {
        ExamDTO request = new ExamDTO();
        request.setCategoryId(1L);

        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_missingCategoryId_returns400() throws Exception {
        ExamDTO request = new ExamDTO();
        request.setName("Exam without category");

        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/exams/{id} ──────────────────────────────────────

    @Test
    void update_existingId_returns200() throws Exception {
        ExamDTO request = new ExamDTO();
        request.setCategoryId(1L);
        request.setName("Updated Exam");
        request.setMaxTimeMinutes(90);
        request.setSuccessPercentage(80);

        ExamDTO updated = new ExamDTO();
        updated.setId(10L);
        updated.setName("Updated Exam");
        updated.setCategoryId(1L);
        updated.setMaxTimeMinutes(90);
        updated.setSuccessPercentage(80);
        updated.setStatus("RELEASED");

        when(examService.update(eq(10L), any(ExamDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/exams/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Exam"))
                .andExpect(jsonPath("$.maxTimeMinutes").value(90));
    }

    @Test
    void update_nonExistingId_returns404() throws Exception {
        ExamDTO request = new ExamDTO();
        request.setCategoryId(1L);
        request.setName("Ghost Exam");

        when(examService.update(eq(99L), any(ExamDTO.class)))
                .thenThrow(new ResourceNotFoundException("Exam", 99L));

        mockMvc.perform(put("/api/exams/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/exams/{id} ───────────────────────────────────

    @Test
    void delete_existingId_returns204() throws Exception {
        doNothing().when(examService).delete(10L);

        mockMvc.perform(delete("/api/exams/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_nonExistingId_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Exam", 99L)).when(examService).delete(99L);

        mockMvc.perform(delete("/api/exams/99"))
                .andExpect(status().isNotFound());
    }
}
