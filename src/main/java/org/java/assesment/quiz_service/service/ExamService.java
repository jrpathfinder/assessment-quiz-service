package org.java.assesment.quiz_service.service;

import lombok.RequiredArgsConstructor;
import org.java.assesment.quiz_service.dto.ExamDTO;
import org.java.assesment.quiz_service.exception.ResourceNotFoundException;
import org.java.assesment.quiz_service.model.Category;
import org.java.assesment.quiz_service.model.Exam;
import org.java.assesment.quiz_service.model.enums.ExamStatus;
import org.java.assesment.quiz_service.repository.CategoryRepository;
import org.java.assesment.quiz_service.repository.ExamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamService {

    private final ExamRepository examRepository;
    private final CategoryRepository categoryRepository;

    public List<ExamDTO> findAll() {
        return examRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public List<ExamDTO> findByCategory(Long categoryId) {
        return examRepository.findByCategoryId(categoryId).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<ExamDTO> findReleasedByCategory(Long categoryId) {
        return examRepository.findByCategoryIdAndStatus(categoryId, ExamStatus.RELEASED).stream()
                .map(this::toDTO)
                .toList();
    }

    public ExamDTO findById(Long id) {
        return toDTO(getOrThrow(id));
    }

    @Transactional
    public ExamDTO create(ExamDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", dto.getCategoryId()));

        Exam exam = new Exam();
        applyFields(exam, dto, category);
        return toDTO(examRepository.save(exam));
    }

    @Transactional
    public ExamDTO update(Long id, ExamDTO dto) {
        Exam exam = getOrThrow(id);
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", dto.getCategoryId()));
        applyFields(exam, dto, category);
        return toDTO(examRepository.save(exam));
    }

    @Transactional
    public void delete(Long id) {
        if (!examRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exam", id);
        }
        examRepository.deleteById(id);
    }

    private void applyFields(Exam exam, ExamDTO dto, Category category) {
        exam.setName(dto.getName());
        exam.setDescription(dto.getDescription());
        exam.setCategory(category);
        exam.setMaxTimeMinutes(dto.getMaxTimeMinutes() != null ? dto.getMaxTimeMinutes() : 60);
        exam.setSuccessPercentage(dto.getSuccessPercentage() != null ? dto.getSuccessPercentage() : 70);
        if (dto.getStatus() != null) {
            exam.setStatus(ExamStatus.valueOf(dto.getStatus()));
        }
    }

    private Exam getOrThrow(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam", id));
    }

    private ExamDTO toDTO(Exam e) {
        ExamDTO dto = new ExamDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setDescription(e.getDescription());
        dto.setCategoryId(e.getCategory().getId());
        dto.setCategoryName(e.getCategory().getName());
        dto.setMaxTimeMinutes(e.getMaxTimeMinutes());
        dto.setSuccessPercentage(e.getSuccessPercentage());
        dto.setStatus(e.getStatus().name());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }
}
