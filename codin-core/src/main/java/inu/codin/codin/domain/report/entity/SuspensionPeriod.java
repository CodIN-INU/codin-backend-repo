package inu.codin.codin.domain.report.entity;

import lombok.Getter;

@Getter
public enum SuspensionPeriod {
    ONE_DAY(1),
    THREE_DAYS(3),
    SEVEN_DAYS(7),
    THIRTY_DAYS(30),
    PERMANENT(Integer.MAX_VALUE); // 영구 정지

    private final int days;

    SuspensionPeriod(Integer days) {
        this.days = days;
    }
}
