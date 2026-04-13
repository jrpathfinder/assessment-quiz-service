package org.java.assesment.quiz_service.service;

import lombok.RequiredArgsConstructor;
import org.java.assesment.quiz_service.dto.PossibleAnswerDTO;
import org.java.assesment.quiz_service.dto.QuestionDTO;
import org.java.assesment.quiz_service.exception.ResourceNotFoundException;
import org.java.assesment.quiz_service.model.Exam;
import org.java.assesment.quiz_service.model.PossibleAnswer;
import org.java.assesment.quiz_service.model.Question;
import org.java.assesment.quiz_service.model.enums.AnswerType;
import org.java.assesment.quiz_service.model.enums.QuestionStatus;
import org.java.assesment.quiz_service.repository.ExamRepository;
import org.java.assesment.quiz_service.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;

    public List<QuestionDTO> findAll() {
        return questionRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public List<QuestionDTO> findByExam(Long examId) {
        return questionRepository.findByExamId(examId).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<QuestionDTO> findReleasedByExam(Long examId) {
        return questionRepository.findByExamIdAndStatus(examId, QuestionStatus.RELEASED).stream()
                .map(this::toDTO)
                .toList();
    }

    public QuestionDTO findById(Long id) {
        return toDTO(getOrThrow(id));
    }

    @Transactional
    public QuestionDTO create(QuestionDTO dto) {
        Exam exam = examRepository.findById(dto.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam", dto.getExamId()));

        Question question = new Question();
        question.setExam(exam);
        applyFields(question, dto);

        // attach answers with back-reference
        for (PossibleAnswerDTO aDto : dto.getPossibleAnswers()) {
            PossibleAnswer answer = buildAnswer(aDto, question);
            question.getPossibleAnswers().add(answer);
        }

        return toDTO(questionRepository.save(question));
    }

    @Transactional
    public QuestionDTO update(Long id, QuestionDTO dto) {
        Question question = getOrThrow(id);
        applyFields(question, dto);

        // replace answers
        question.getPossibleAnswers().clear();
        for (PossibleAnswerDTO aDto : dto.getPossibleAnswers()) {
            question.getPossibleAnswers().add(buildAnswer(aDto, question));
        }

        return toDTO(questionRepository.save(question));
    }

    @Transactional
    public void delete(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question", id);
        }
        questionRepository.deleteById(id);
    }

    private void applyFields(Question q, QuestionDTO dto) {
        q.setQuestionText(dto.getQuestionText());
        q.setExplanation(dto.getExplanation());
        if (dto.getStatus() != null) {
            q.setStatus(QuestionStatus.valueOf(dto.getStatus()));
        }
        if (dto.getAnswerType() != null) {
            q.setAnswerType(AnswerType.valueOf(dto.getAnswerType()));
        }
    }

    private PossibleAnswer buildAnswer(PossibleAnswerDTO aDto, Question question) {
        PossibleAnswer a = new PossibleAnswer();
        a.setQuestion(question);
        a.setText(aDto.getText());
        a.setCorrect(aDto.isCorrect());
        a.setOrderIndex(aDto.getOrderIndex() != null ? aDto.getOrderIndex() : 0);
        return a;
    }

    private Question getOrThrow(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));
    }

    private QuestionDTO toDTO(Question q) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(q.getId());
        dto.setExamId(q.getExam().getId());
        dto.setQuestionText(q.getQuestionText());
        dto.setExplanation(q.getExplanation());
        dto.setStatus(q.getStatus().name());
        dto.setAnswerType(q.getAnswerType().name());
        dto.setCreatedAt(q.getCreatedAt());
        dto.setUpdatedAt(q.getUpdatedAt());

        List<PossibleAnswerDTO> answers = q.getPossibleAnswers().stream()
                .map(a -> {
                    PossibleAnswerDTO aDto = new PossibleAnswerDTO();
                    aDto.setId(a.getId());
                    aDto.setText(a.getText());
                    aDto.setCorrect(a.isCorrect());
                    aDto.setOrderIndex(a.getOrderIndex());
                    return aDto;
                })
                .toList();
        dto.setPossibleAnswers(answers);
        return dto;
    }
}
