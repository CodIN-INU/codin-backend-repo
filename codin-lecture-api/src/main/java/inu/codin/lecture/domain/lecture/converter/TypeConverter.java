package inu.codin.lecture.domain.lecture.converter;

import inu.codin.lecture.domain.lecture.entity.Type;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TypeConverter implements AttributeConverter<Type, String> {
    @Override
    public String convertToDatabaseColumn(Type attribute) {
        return attribute.getDescription();
    }

    @Override
    public Type convertToEntityAttribute(String dbData) {
        return Type.fromDescription(dbData);
    }
}
