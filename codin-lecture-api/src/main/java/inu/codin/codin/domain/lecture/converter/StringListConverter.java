package inu.codin.codin.domain.lecture.converter;

import inu.codin.codin.domain.lecture.exception.LectureErrorCode;
import inu.codin.codin.domain.lecture.exception.LectureException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Converter(autoApply = true)
public class StringListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> dataList) {
        try {
            return String.join(",", dataList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new LectureException(LectureErrorCode.CONVERT_ERROR);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String data) {
        try {
            return data != null ? Arrays.stream(data.split(",")).toList() : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new LectureException(LectureErrorCode.CONVERT_ERROR);
        }
    }
}
