package org.java.assesment.quiz_service.repository;

import org.java.assesment.quiz_service.model.PossibleAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PossibleAnswerRepository extends JpaRepository<PossibleAnswer, Long> {

    List<PossibleAnswer> findByQuestionIdOrderByOrderIndexAsc(Long questionId);
}
