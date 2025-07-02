package inu.codin.codin.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@Slf4j
public class ReportCountResponseDto {
    @Schema(description = "report target id", example = "targetId1")
    @NotNull
    private String reportTargetId;  // reportTargetId

    @Schema(description = "신고 개수", example = "5")
    @NotNull
    private Integer count;  // 신고 개수

    @Schema(description = "신고 목록", example = "[...]")
    @NotNull
    private List<ReportResponseDto> reports;  // 신고 목록

    // 팩토리 메서드 추가
    @Builder
    public static ReportCountResponseDto from(Document document) {
        String targetId = document.getObjectId("_id").toString();
        Integer count = document.getInteger("count");

        log.info("document: {}" , document);
        List<Document> reportDocs = document.getList("reports", Document.class);
        log.info("reportDocs: {}" ,reportDocs);

        List<ReportResponseDto> reports = reportDocs.stream()
                .map(ReportResponseDto::fromDoc)
                .collect(Collectors.toList());
        log.info("reports: {}" ,reports);

        return ReportCountResponseDto.builder()
                .reportTargetId(targetId)
                .count(count)
                .reports(reports)
                .build();
    }

}
