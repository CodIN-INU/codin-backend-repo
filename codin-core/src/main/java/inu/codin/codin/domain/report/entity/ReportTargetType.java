package inu.codin.codin.domain.report.entity;

import lombok.Getter;

@Getter
public enum ReportTargetType {
    USER("사용자"),
    POST("게시물"),
    COMMENT("댓글"),
    REPLY("대댓글");

    private final String description;

    ReportTargetType(String description) {
        this.description = description;
    }
}
