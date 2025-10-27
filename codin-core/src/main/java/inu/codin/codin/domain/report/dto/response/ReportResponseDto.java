package inu.codin.codin.domain.report.dto.response;

import inu.codin.codin.domain.report.entity.ReportEntity;
import inu.codin.codin.domain.report.entity.ReportEntity.ReportActionEntity;
import inu.codin.codin.domain.report.entity.ReportStatus;
import inu.codin.codin.domain.report.entity.ReportTargetType;
import inu.codin.codin.domain.report.entity.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.bson.Document;

@Getter
@Builder
public class ReportResponseDto {

    @Schema(description = "report id", example = "1")
    @NotNull
    private String _id;

    @Schema(description = "신고한 유저", example = "2")
    @NotNull
    private String reportingUserId;

    @Schema(description = "신고당한 유저", example = "2")
    @NotNull
    private String reportedUserId;

    @Schema(description = "신고할 대상 타입", example = "USER, POST, COMMENT, REPLY")
    @NotNull
    private ReportTargetType reportTargetType;

    @Schema(description = "신고 대상 타입의 ID (유저, 게시물, 댓글, 대댓글)", example = "2")
    @NotNull
    private String reportTargetId;

    @Schema(description = "신고 유형 (게시글 부적절, 스팸, ...)", example = "INAPPROPRIATE_CONTENT, COMMERCIAL_AD , ABUSE , OBSCENE, POLITICAL, FRAUD , SPAM")
    @NotNull
    private ReportType reportType;


    @Schema(description = "신고 처리 상태 Pending <-> Reloved", example = "Pending, Resolved")
    @NotNull
    private ReportStatus reportStatus;


    @Schema(description = "처리 정보 (ReportActionEntity)", example = "")
    @NotNull
    private ReportActionDto action;

    @Getter
    public static class ReportActionDto {
        // 신고 처리 관리자 id
        private String actionTakenById;

        // 신고에 대한 코멘트
        //private String comment;

        // 정지 기간 Enum
        private String suspensionPeriod;

        // 정지 종료일
        private String suspensionEndDate;

        // DTO 생성자 (ReportActionEntity로부터 데이터를 변환)
        public ReportActionDto(ReportActionEntity actionEntity) {
            if (actionEntity != null) {
                this.actionTakenById = actionEntity.getActionTakenById().toString();
                this.suspensionPeriod = actionEntity.getSuspensionPeriod() != null ? actionEntity.getSuspensionPeriod().name() : null;
                this.suspensionEndDate = actionEntity.getSuspensionEndDate() != null ? actionEntity.getSuspensionEndDate().toString() : null;
            }
        }
    }

    // ReportEntity를 ReportResponseDto로 변환
    public static ReportResponseDto from(ReportEntity reportEntity) {
        return ReportResponseDto.builder()
                ._id(reportEntity.get_id().toString())
                .reportingUserId(reportEntity.getReportingUserId().toString())
                .reportedUserId(reportEntity.getReportedUserId().toString())
                .reportTargetType(reportEntity.getReportTargetType())
                .reportTargetId(reportEntity.getReportTargetId().toString())
                .reportType(reportEntity.getReportType())
                .reportStatus(reportEntity.getReportStatus())
                .action(reportEntity.getAction() != null ? new ReportActionDto(reportEntity.getAction()) : null)
                .build();
    }

    // Document를 ReportResponseDto로 변환
    public static ReportResponseDto fromDoc(Document doc) {
        return ReportResponseDto.builder()
                ._id(doc.getObjectId("_id").toString())
                .reportingUserId(doc.getObjectId("reportingUserId").toString())
                .reportedUserId(doc.getObjectId("reportedUserId").toString())
                .reportTargetType(ReportTargetType.valueOf(doc.getString("reportTargetType")))
                .reportTargetId(doc.getObjectId("reportTargetId").toString())
                .reportType(ReportType.valueOf(doc.getString("reportType")))  // Enum 변환
                .reportStatus(ReportStatus.valueOf(doc.getString("reportStatus")))  // Enum 변환
                .build();
    }
}
