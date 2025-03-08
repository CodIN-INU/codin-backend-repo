package inu.codin.codin.domain.report.dto;

import inu.codin.codin.domain.report.entity.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReportInfo {
    private String reportedEntityId; // 신고 대상 엔터티 ID
    private int reportCount; // 신고 누적 횟수
    private ReportTargetType entityType; // 신고 대상 타입 (POST, COMMENT, REPLY)
    private String userId; // 해당 엔터티의 작성자 ID
}
