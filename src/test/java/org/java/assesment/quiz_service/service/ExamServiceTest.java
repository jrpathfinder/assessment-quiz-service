package org.java.assesment.quiz_service.service;

import org.java.assesment.quiz_service.dto.ExamDTO;
import org.java.assesment.quiz_service.exception.ResourceNotFoundException;
import org.java.assesment.quiz_service.model.Category;
import org.java.assesment.quiz_service.model.Exam;
import org.java.assesment.quiz_service.model.enums.ExamStatus;
import org.java.assesment.quiz_service.repository.CategoryRepository;
import org.java.assesment.quiz_service.repository.ExamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock private ExamRepository examRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private ExamService examService;

    private Category javaCategory;
    private Exam javaExam;

    @BeforeEach
    void setUp() {
        javaCategory = new Category();
        javaCategory.setId(1L);
        javaCategory.setName("Java");
        javaCategory.setSlug("java");

        javaExam = new Exam();
        javaExam.setId(10L);
        javaExam.setName("Java Core – Junior");
        javaExam.setDescription("Basic Java exam");
        javaExam.setCategory(javaCategory);
        javaExam.setMaxTimeMinutes(60);
        javaExam.setSuccessPercentage(70);
        javaExam.setStatus(ExamStatus.DRAFT);
        javaExam.setCreatedAt(LocalDateTime.now());
        javaExam.setUpdatedAt(LocalDateTime.now());
    }

    // ── findAll ──────────────────────────────────────────────────

    @Test
    void findAll_returnsAllExams() {
        when(examRepository.findAll()).thenReturn(List.of(javaExam));

        List<ExamDTO> result = examService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Java Core – Junior");
        assertThat(result.get(0).getCategoryId()).isEqualTo(1L);
        assertThat(result.get(0).getCategoryName()).isEqualTo("Java");
    }

    // ── findByCategory ───────────────────────────────────────────

    @Test
    void findByCategory_returnsExamsForCategory() {
        when(examRepository.findByCategoryId(1L)).thenReturn(List.of(javaExam));

        List<ExamDTO> result = examService.findByCategory(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryId()).isEqualTo(1L);
    }

    @Test
    void findByCategory_noExams_returnsEmpty() {
        when(examRepository.findByCategoryId(99L)).thenReturn(List.of());

        assertThat(examService.findByCategory(99L)).isEmpty();
    }

    // ── findById ─────────────────────────────────────────────────

    @Test
    void findById_existingId_returnsDTO() {
        when(examRepository.findById(10L)).thenReturn(Optional.of(javaExam));

        ExamDTO dto = examService.findById(10L);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getStatus()).isEqualTo("DRAFT");
        assertThat(dto.getMaxTimeMinutes()).isEqualTo(60);
        assertThat(dto.getSuccessPercentage()).isEqualTo(70);
    }

    @Test
    void findById_nonExistingId_throws() {
        when(examRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> examService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── create ───────────────────────────────────────────────────

    @Test
    void create_validDTO_savesExam() {
        ExamDTO dto = new ExamDTO();
        dto.setCategoryId(1L);
        dto.setName("Java Core – Senior");
        dto.setMaxTimeMinutes(90);
        dto.setSuccessPercentage(80);
        dto.setStatus("BETA");

        Exam saved = new Exam();
        saved.setId(11L);
        saved.setName(dto.getName());
        saved.setCategory(javaCategory);
        saved.setMaxTimeMinutes(90);
        saved.setSuccessPercentage(80);
        saved.setStatus(ExamStatus.BETA);
        saved.setCreatedAt(LocalDateTime.now());
        saved.setUpdatedAt(LocalDateTime.now());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(javaCategory));
        when(examRepository.save(any(Exam.class))).thenReturn(saved);

        ExamDTO result = examService.create(dto);

        assertThat(result.getId()).isEqualTo(11L);
        assertThat(result.getStatus()).isEqualTo("BETA");
        verify(examRepository).save(any(Exam.class));
    }

    @Test
    void create_categoryNotFound_throws() {
        ExamDTO dto = new ExamDTO();
        dto.setCategoryId(99L);
        dto.setName("Orphan Exam");

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> examService.create(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_nullMaxTime_usesDefault60() {
        ExamDTO dto = new ExamDTO();
        dto.setCategoryId(1L);
        dto.setName("Exam with defaults");
        dto.setMaxTimeMinutes(null);
        dto.setSuccessPercentage(null);

        Exam saved = new Exam();
        saved.setId(12L);
        saved.setName(dto.getName());
        saved.setCategory(javaCategory);
        saved.setMaxTimeMinutes(60);
        saved.setSuccessPercentage(70);
        saved.setStatus(ExamStatus.DRAFT);
        saved.setCreatedAt(LocalDateTime.now());
        saved.setUpdatedAt(LocalDateTime.now());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(javaCategory));
        when(examRepository.save(any(Exam.class))).thenReturn(saved);

        ExamDTO result = examService.create(dto);

        assertThat(result.getMaxTimeMinutes()).isEqualTo(60);
        assertThat(result.getSuccessPercentage()).isEqualTo(70);
    }

    // ── update ───────────────────────────────────────────────────

    @Test
    void update_existingExam_updatesAllFields() {
        ExamDTO updateDto = new ExamDTO();
        updateDto.setCategoryId(1L);
        updateDto.setName("Updated Name");
        updateDto.setMaxTimeMinutes(120);
        updateDto.setSuccessPercentage(75);
        updateDto.setStatus("RELEASED");

        Exam updated = new Exam();
        updated.setId(10L);
        updated.setName("Updated Name");
        updated.setCategory(javaCategory);
        updated.setMaxTimeMinutes(120);
        updated.setSuccessPercentage(75);
        updated.setStatus(ExamStatus.RELEASED);
        updated.setCreatedAt(javaExam.getCreatedAt());
        updated.setUpdatedAt(LocalDateTime.now());

        when(examRepository.findById(10L)).thenReturn(Optional.of(javaExam));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(javaCategory));
        when(examRepository.save(any(Exam.class))).thenReturn(updated);

        ExamDTO result = examService.update(10L, updateDto);

        assertThat(result.getStatus()).isEqualTo("RELEASED");
        assertThat(result.getMaxTimeMinutes()).isEqualTo(120);
    }

    @Test
    void update_nonExistingExam_throws() {
        ExamDTO dto = new ExamDTO();
        dto.setCategoryId(1L);

        when(examRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> examService.update(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── delete ───────────────────────────────────────────────────

    @Test
    void delete_existingExam_deletes() {
        when(examRepository.existsById(10L)).thenReturn(true);

        examService.delete(10L);

        verify(examRepository).deleteById(10L);
    }

    @Test
    void delete_nonExistingExam_throws() {
        when(examRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> examService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(examRepository, never()).deleteById(any());
    }
}
