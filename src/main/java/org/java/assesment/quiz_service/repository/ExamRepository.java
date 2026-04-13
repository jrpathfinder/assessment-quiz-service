package org.java.assesment.quiz_service.repository;

import org.java.assesment.quiz_service.model.Exam;
import org.java.assesment.quiz_service.model.enums.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByCategoryId(Long categoryId);

    List<Exam> findByCategoryIdAndStatus(Long categoryId, ExamStatus status);

    List<Exam> findByStatus(ExamStatus status);
}
