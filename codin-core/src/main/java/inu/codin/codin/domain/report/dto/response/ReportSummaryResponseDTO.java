package inu.codin.codin.domain.report.dto.response;

import inu.codin.codin.domain.report.entity.ReportType;
import lombok.Getter;

import java.util.Map;

@Getter
public class ReportSummaryResponseDTO {
    private final Map<ReportType, Integer> reportTypeCounts;

    public ReportSummaryResponseDTO(Map<ReportType, Integer> reportTypeCounts) {
        this.reportTypeCounts = reportTypeCounts;
    }
}


