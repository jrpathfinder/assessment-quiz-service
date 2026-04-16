package org.java.assesment.quiz_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java.assesment.quiz_service.dto.CategoryDTO;
import org.java.assesment.quiz_service.exception.ResourceNotFoundException;
import org.java.assesment.quiz_service.service.CategoryService;
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
class CategoryControllerTest {

    @Mock  private CategoryService categoryService;
    @InjectMocks private CategoryController categoryController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    private CategoryDTO javaDto;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setValidator(validator)
                .build();

        javaDto = new CategoryDTO();
        javaDto.setId(1L);
        javaDto.setName("Java");
        javaDto.setSlug("java");
        javaDto.setDescription("Core Java");
        javaDto.setCreatedAt(LocalDateTime.now());
        javaDto.setUpdatedAt(LocalDateTime.now());
    }

    // ── GET /api/categories ──────────────────────────────────────

    @Test
    void getAll_returns200WithList() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of(javaDto));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[0].slug").value("java"));
    }

    @Test
    void getAll_emptyList_returns200WithEmptyArray() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/categories/{id} ─────────────────────────────────

    @Test
    void getById_existingId_returns200() throws Exception {
        when(categoryService.findById(1L)).thenReturn(javaDto);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Java"));
    }

    @Test
    void getById_nonExistingId_returns404() throws Exception {
        when(categoryService.findById(99L)).thenThrow(new ResourceNotFoundException("Category", 99L));

        mockMvc.perform(get("/api/categories/99"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/categories ─────────────────────────────────────

    @Test
    void create_validBody_returns201WithDTO() throws Exception {
        CategoryDTO request = new CategoryDTO();
        request.setName("Spring Boot");
        request.setSlug("spring-boot");
        request.setDescription("Spring ecosystem");

        CategoryDTO response = new CategoryDTO();
        response.setId(2L);
        response.setName("Spring Boot");
        response.setSlug("spring-boot");

        when(categoryService.create(any(CategoryDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Spring Boot"));
    }

    @Test
    void create_missingName_returns400() throws Exception {
        CategoryDTO request = new CategoryDTO();
        request.setSlug("no-name");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_missingSlug_returns400() throws Exception {
        CategoryDTO request = new CategoryDTO();
        request.setName("No Slug");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/categories/{id} ─────────────────────────────────

    @Test
    void update_existingId_returns200() throws Exception {
        CategoryDTO request = new CategoryDTO();
        request.setName("Java Updated");
        request.setSlug("java-updated");

        CategoryDTO updated = new CategoryDTO();
        updated.setId(1L);
        updated.setName("Java Updated");
        updated.setSlug("java-updated");

        when(categoryService.update(eq(1L), any(CategoryDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Java Updated"));
    }

    @Test
    void update_nonExistingId_returns404() throws Exception {
        CategoryDTO request = new CategoryDTO();
        request.setName("Ghost");
        request.setSlug("ghost");

        when(categoryService.update(eq(99L), any(CategoryDTO.class)))
                .thenThrow(new ResourceNotFoundException("Category", 99L));

        mockMvc.perform(put("/api/categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/categories/{id} ──────────────────────────────

    @Test
    void delete_existingId_returns204() throws Exception {
        doNothing().when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService).delete(1L);
    }

    @Test
    void delete_nonExistingId_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Category", 99L)).when(categoryService).delete(99L);

        mockMvc.perform(delete("/api/categories/99"))
                .andExpect(status().isNotFound());
    }
}
