package org.java.assesment.quiz_service.service;

import org.java.assesment.quiz_service.dto.CategoryDTO;
import org.java.assesment.quiz_service.exception.ResourceNotFoundException;
import org.java.assesment.quiz_service.model.Category;
import org.java.assesment.quiz_service.repository.CategoryRepository;
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
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category javaCategory;

    @BeforeEach
    void setUp() {
        javaCategory = new Category();
        javaCategory.setId(1L);
        javaCategory.setName("Java");
        javaCategory.setSlug("java");
        javaCategory.setDescription("Core Java");
        javaCategory.setCreatedAt(LocalDateTime.now());
        javaCategory.setUpdatedAt(LocalDateTime.now());
    }

    // ── findAll ──────────────────────────────────────────────────

    @Test
    void findAll_returnsAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(javaCategory));

        List<CategoryDTO> result = categoryService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Java");
        assertThat(result.get(0).getSlug()).isEqualTo("java");
    }

    @Test
    void findAll_emptyList_returnsEmpty() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        assertThat(categoryService.findAll()).isEmpty();
    }

    // ── findById ─────────────────────────────────────────────────

    @Test
    void findById_existingId_returnsDTO() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(javaCategory));

        CategoryDTO dto = categoryService.findById(1L);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Java");
        assertThat(dto.getDescription()).isEqualTo("Core Java");
    }

    @Test
    void findById_nonExistingId_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── create ───────────────────────────────────────────────────

    @Test
    void create_validDTO_savesAndReturnsDTO() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Spring Boot");
        dto.setSlug("spring-boot");
        dto.setDescription("Spring ecosystem");

        Category saved = new Category();
        saved.setId(2L);
        saved.setName(dto.getName());
        saved.setSlug(dto.getSlug());
        saved.setDescription(dto.getDescription());
        saved.setCreatedAt(LocalDateTime.now());
        saved.setUpdatedAt(LocalDateTime.now());

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryDTO result = categoryService.create(dto);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Spring Boot");
        assertThat(result.getSlug()).isEqualTo("spring-boot");
        verify(categoryRepository).save(any(Category.class));
    }

    // ── update ───────────────────────────────────────────────────

    @Test
    void update_existingId_updatesFields() {
        CategoryDTO updateDto = new CategoryDTO();
        updateDto.setName("Java Updated");
        updateDto.setSlug("java-updated");
        updateDto.setDescription("Updated description");

        Category updated = new Category();
        updated.setId(1L);
        updated.setName("Java Updated");
        updated.setSlug("java-updated");
        updated.setDescription("Updated description");
        updated.setCreatedAt(javaCategory.getCreatedAt());
        updated.setUpdatedAt(LocalDateTime.now());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(javaCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(updated);

        CategoryDTO result = categoryService.update(1L, updateDto);

        assertThat(result.getName()).isEqualTo("Java Updated");
        assertThat(result.getSlug()).isEqualTo("java-updated");
    }

    @Test
    void update_nonExistingId_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(99L, new CategoryDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── delete ───────────────────────────────────────────────────

    @Test
    void delete_existingId_deletesCategory() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void delete_nonExistingId_throwsResourceNotFoundException() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).deleteById(any());
    }
}
