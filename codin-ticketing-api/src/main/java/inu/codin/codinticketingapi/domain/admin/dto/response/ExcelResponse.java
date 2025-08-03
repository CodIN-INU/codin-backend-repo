package inu.codin.codinticketingapi.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ExcelResponse {
    private byte[] excel;
    private String fileName;

    public static ExcelResponse of(byte[] excel, String fileName) {
        return ExcelResponse.builder()
                .excel(excel)
                .fileName(fileName)
                .build();
    }
}
