package inu.codin.codin.domain.board.question.service;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.domain.board.question.dto.request.QuestionCreateUpdateRequestDto;
import inu.codin.codin.domain.board.question.dto.response.QuestionResponseDto;
import inu.codin.codin.domain.board.question.entity.QuestionEntity;
import inu.codin.codin.domain.board.question.exception.QuestionErrorCode;
import inu.codin.codin.domain.board.question.exception.QuestionException;
import inu.codin.codin.domain.board.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    public List<QuestionResponseDto> getAllQuestions(Department department) {
        validateDepartment(department);
        return questionRepository.findAllByDepartment(department)
                .stream().map(QuestionResponseDto::of).toList();
    }

    public void createQuestion(QuestionCreateUpdateRequestDto requestDto) {
        validateDepartment(requestDto.getDepartment());

        QuestionEntity questionEntity = QuestionEntity.builder()
                .question(requestDto.getQuestion())
                .answer(requestDto.getAnswer())
                .department(requestDto.getDepartment())
                .build();
        questionRepository.save(questionEntity);
    }


    public void updateQuestion(String id, QuestionCreateUpdateRequestDto requestDto) {
        validateDepartment(requestDto.getDepartment());
        QuestionEntity questionEntity = questionRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new QuestionException(QuestionErrorCode.QUESTION_NOT_FOUND));

        questionEntity.updateQuestion(requestDto);
        questionRepository.save(questionEntity);
    }

    public void deleteQuestion(String id) {
        QuestionEntity questionEntity = questionRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new QuestionException(QuestionErrorCode.QUESTION_NOT_FOUND));
        questionRepository.delete(questionEntity);
    }

    private void validateDepartment(Department department) {
        if (!(department.equals(Department.COMPUTER_SCI) || department.equals(Department.INFO_COMM) || department.equals(Department.EMBEDDED) || department.equals(Department.IT_COLLEGE))) {
            throw new QuestionException(QuestionErrorCode.INVALID_DEPARTMENT);
        }
    }
}
