package inu.codin.codin.domain.report.dto.response;

import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO;
import inu.codin.codin.domain.report.dto.ReportInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReportListResponseDto {
    private final PostDetailResponseDTO post;
    private final ReportInfo reportInfo;

    @Builder
    public ReportListResponseDto(PostDetailResponseDTO post, ReportInfo reportInfo) {
        this.post = post;
        this.reportInfo = reportInfo;
    }

    public static ReportListResponseDto from(PostDetailResponseDTO post, ReportInfo reportInfo) {
        return ReportListResponseDto.builder()
                .post(post)
                .reportInfo(reportInfo)
                .build();
    }
}