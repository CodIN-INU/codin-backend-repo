package inu.codin.codin.domain.lecture.converter;

import inu.codin.codin.domain.lecture.entity.Evaluation;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EvaluationConverter implements AttributeConverter<Evaluation, String> {
    @Override
    public String convertToDatabaseColumn(Evaluation attribute) {
        return attribute.getDescription();
    }

    @Override
    public Evaluation convertToEntityAttribute(String dbData) {
        return Evaluation.fromDescription(dbData);
    }
}