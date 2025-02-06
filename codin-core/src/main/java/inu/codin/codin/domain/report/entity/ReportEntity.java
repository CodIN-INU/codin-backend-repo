package inu.codin.codin.domain.report.entity;


import inu.codin.codin.common.BaseTimeEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reports")
@Getter
public class ReportEntity extends BaseTimeEntity {
    @Id
    @NotBlank
    private ObjectId _id;

    //신고한 유저
    private ObjectId reportingUserId;

    //신고당한 유저
    private ObjectId reportedUserId;

    //신고 대상 타입 ( 유저, 게시물, 댓글, 대댓글)
    private ReportTargetType reportTargetType;

    //신고 대상 ID ( 유저, 게시물, 댓글, 대댓글)
    private ObjectId reportTargetId;

    //신고 유형 ( 게시글 부적절, 스팸 ,,.)
    private ReportType reportType;

    //신고 처리 상태 Pending <-> Reloved
    private ReportStatus reportStatus;

    private ReportActionEntity action; // 처리 정보

    @Builder
    public ReportEntity(ObjectId reportingUserId, ObjectId reportedUserId, ReportTargetType reportTargetType,
                        ObjectId reportTargetId, ReportType reportType, ReportStatus reportStatus,
                        ReportActionEntity action) {
        this.reportingUserId = reportingUserId;
        this.reportedUserId = reportedUserId;
        this.reportTargetType = reportTargetType;
        this.reportTargetId = reportTargetId;
        this.reportType = reportType;
        // Null이면 PENDING, 아니면 그대로 유지
        this.reportStatus = reportStatus != null ? reportStatus : ReportStatus.PENDING;
        this.action = action;
    }

    // 신고 상태 업데이트 (빌더 패턴 활용)
    public ReportEntity updateReport(ReportActionEntity action) {
        return ReportEntity.builder()
                .reportingUserId(this.reportingUserId)
                .reportedUserId(this.reportedUserId)
                .reportTargetType(this.reportTargetType)
                .reportTargetId(this.reportTargetId)
                .reportType(this.reportType)
                .reportStatus(ReportStatus.SUSPENDED) // 상태 변경
                .action(action) // 신고 처리 정보 업데이트
                .build();
    }

    public void updateReportSuspended(ReportActionEntity action) {
        this.reportStatus = ReportStatus.SUSPENDED; // 상태 변경
        this.action = action; // 신고 처리 정보 업데이트
    }


    public void updateReportResolved() {
        this.reportStatus = ReportStatus.RESOLVED; // 상태 변경
    }




    @Getter
    public static class ReportActionEntity extends BaseTimeEntity {
        //신고 처리 관리자 id
        private ObjectId actionTakenById;

        //신고에 대한 코멘트
        private String comment;

        // 정지 기간 Enum
        private SuspensionPeriod suspensionPeriod;

        // 정지 종료일
        private LocalDateTime suspensionEndDate;

        @Builder
        public ReportActionEntity(ObjectId actionTakenById, String comment,
                                  SuspensionPeriod suspensionPeriod, LocalDateTime suspensionEndDate) {
            this.actionTakenById = actionTakenById;
            this.comment = comment;
            this.suspensionPeriod = suspensionPeriod;
            this.suspensionEndDate = suspensionEndDate;
        }
    }
}
