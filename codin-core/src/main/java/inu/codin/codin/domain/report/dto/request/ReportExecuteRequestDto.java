package inu.codin.codin.domain.report.dto.request;

import inu.codin.codin.domain.report.entity.ReportStatus;
import inu.codin.codin.domain.report.entity.SuspensionPeriod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReportExecuteRequestDto {
    private String reportTargetId; // 신고 ID
    private SuspensionPeriod suspensionPeriod; // 정지 기간

}
