package inu.codin.codin.domain.board.question.exception;

import inu.codin.codin.common.exception.GlobalException;
import lombok.Getter;
@Getter
public class QuestionException extends GlobalException {

    private final QuestionErrorCode errorCode;
    public QuestionException(QuestionErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
