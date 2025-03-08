package inu.codin.codin.domain.post.dto.response;


import inu.codin.codin.domain.report.dto.ReportInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostReportResponse extends PostDetailResponseDTO {

    private final ReportInfo reportInfo;

    @Builder
    public PostReportResponse(PostDetailResponseDTO baseDTO, ReportInfo reportInfo) {
        super(baseDTO.getUserId(), baseDTO.get_id(), baseDTO.getTitle(), baseDTO.getContent(), baseDTO.getNickname(),
                baseDTO.getPostCategory(), baseDTO.getUserImageUrl(), baseDTO.getPostImageUrl(), baseDTO.isAnonymous(),
                baseDTO.getLikeCount(), baseDTO.getScrapCount(), baseDTO.getHits(), baseDTO.getCreatedAt(),
                baseDTO.getCommentCount(), baseDTO.getUserInfo());
        this.reportInfo = reportInfo;
    }


    public static PostReportResponse from(PostDetailResponseDTO base, ReportInfo reportInfo) {
        return PostReportResponse.builder()
                .baseDTO(base)
                .reportInfo(reportInfo)
                .build();
    }
}
