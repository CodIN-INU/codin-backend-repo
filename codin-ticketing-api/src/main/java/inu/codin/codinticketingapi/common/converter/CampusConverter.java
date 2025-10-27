package inu.codin.codinticketingapi.common.converter;

import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CampusConverter implements Converter<String, Campus> {

    @Override
    public Campus convert(String source) {
        return Campus.fromDescription(source.trim());
    }
}
