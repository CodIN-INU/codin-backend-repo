package inu.codin.codin.domain.report.entity;

import lombok.Getter;

@Getter
public enum ReportType {
    INAPPROPRIATE_CONTENT("게시판 성격에 부적절"),
    POLITICAL("부적절한 정치적 의견"),
    FRAUD("사칭/사기"),
    SPAM("도배/낚시"),
    COMMERCIAL_AD("상업적 광고"),
    ABUSE("욕설"),
    OBSCENE("음란물 및 불건전함");


    private final String description;

    ReportType(String description) {
        this.description = description;
    }
}
