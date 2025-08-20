package inu.codin.codin.domain.board.question.dto.response;

import inu.codin.codin.domain.board.question.entity.QuestionEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuestionResponseDto {

    private String id;
    private String question;
    private String answer;

    public static QuestionResponseDto of(QuestionEntity questionEntity) {
        return new QuestionResponseDto(
                questionEntity.get_id().toString(),
                questionEntity.getQuestion(),
                questionEntity.getAnswer()
        );
    }
}
