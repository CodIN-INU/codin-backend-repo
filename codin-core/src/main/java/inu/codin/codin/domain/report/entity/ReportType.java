package inu.codin.codin.domain.report.entity;

import lombok.Getter;

@Getter
public enum ReportType {
    POLITICAL("정치 및 선거운동"),
    ABUSE("욕설 및 폭력"),
    FRAUD("사기 및 사칭"),
    SPAM("도배 및 불쾌함"),
    COMMERCIAL_AD("홍보 및 부적절"),
    OBSCENE("음란물 및 불건전"),
    ETC("기타");


    private final String description;

    ReportType(String description) {
        this.description = description;
    }
}
