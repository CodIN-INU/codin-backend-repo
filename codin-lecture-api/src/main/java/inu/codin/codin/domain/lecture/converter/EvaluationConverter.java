package inu.codin.codin.domain.lecture.converter;

import inu.codin.codin.domain.lecture.entity.Evaluation;
import inu.codin.codin.domain.lecture.exception.LectureErrorCode;
import inu.codin.codin.domain.lecture.exception.LectureException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter(autoApply = true)
public class EvaluationConverter implements AttributeConverter<Evaluation, String> {
    @Override
    public String convertToDatabaseColumn(Evaluation attribute) {
        return attribute != null ? attribute.getDescription() : null;
    }

    @Override
    public Evaluation convertToEntityAttribute(String data) {
        if (data == null || data.trim().isEmpty()) {
            return null;
        }

        try {
            return Evaluation.fromDescription(data.trim());
        } catch (IllegalArgumentException e) {
            log.warn("Evaluation Converter 부분에서 문제가 발생했습니다.");
            throw new LectureException(LectureErrorCode.CONVERT_ERROR);
        }
    }
}