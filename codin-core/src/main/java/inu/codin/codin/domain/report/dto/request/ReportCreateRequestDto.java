package inu.codin.codin.domain.report.dto.request;

import inu.codin.codin.domain.report.entity.ReportTargetType;
import inu.codin.codin.domain.report.entity.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReportCreateRequestDto {

    @Schema(description = "신고할 대상 타입", example = "USER, POST, COMMENT, REPLY")
    @NotNull
    private ReportTargetType reportTargetType;

    @Schema(description = "신고 대상 타입의 ID ( 유저, 게시물, 댓글, 대댓글)", example = "2")
    @NotBlank
    private String reportTargetId;

    @Schema(description = " 신고 유형 ( 게시글 부적절, 스팸 ,,.)", example = "FRAUD, ABUSE, COMMERCIAL_AD, OBSCENE, POLITICAL, ETC, SPAM")
    @NotNull
    private ReportType reportType;
}
