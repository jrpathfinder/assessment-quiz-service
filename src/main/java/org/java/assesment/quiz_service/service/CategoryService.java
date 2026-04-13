package org.java.assesment.quiz_service.service;

import lombok.RequiredArgsConstructor;
import org.java.assesment.quiz_service.dto.CategoryDTO;
import org.java.assesment.quiz_service.exception.ResourceNotFoundException;
import org.java.assesment.quiz_service.model.Category;
import org.java.assesment.quiz_service.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> findAll() {
        return categoryRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public CategoryDTO findById(Long id) {
        return toDTO(getOrThrow(id));
    }

    @Transactional
    public CategoryDTO create(CategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setSlug(dto.getSlug());
        category.setDescription(dto.getDescription());
        return toDTO(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryDTO dto) {
        Category category = getOrThrow(id);
        category.setName(dto.getName());
        category.setSlug(dto.getSlug());
        category.setDescription(dto.getDescription());
        return toDTO(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", id);
        }
        categoryRepository.deleteById(id);
    }

    private Category getOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    private CategoryDTO toDTO(Category c) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setSlug(c.getSlug());
        dto.setDescription(c.getDescription());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }
}
