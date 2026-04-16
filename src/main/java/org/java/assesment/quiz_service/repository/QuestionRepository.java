package org.java.assesment.quiz_service.repository;

import org.java.assesment.quiz_service.model.Question;
import org.java.assesment.quiz_service.model.enums.QuestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByExamId(Long examId);

    List<Question> findByExamIdAndStatus(Long examId, QuestionStatus status);

    long countByExamId(Long examId);
}
