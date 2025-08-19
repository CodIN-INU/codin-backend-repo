package inu.codin.codin.domain.board.question.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.common.dto.Department;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "often_asked_questions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionEntity extends BaseTimeEntity {

    @Id
    private ObjectId _id;

    private String question;

    private String answer;

    private Department department;

    @Builder
    public QuestionEntity(String question, String answer, Department department) {
        this.question = question;
        this.answer = answer;
        this.department = department;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
