package inu.codin.codin.domain.report.dto.response;

import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO;
import inu.codin.codin.domain.report.dto.ReportInfo;
import lombok.Builder;
import lombok.Getter;

@Getter

public class ReportListResponseDto extends PostDetailResponseDTO {

    private final ReportInfo reportInfo;

    @Builder
    public ReportListResponseDto(PostDetailResponseDTO baseDTO, ReportInfo reportInfo) {
        super(baseDTO.getUserId(), baseDTO.get_id(), baseDTO.getTitle(), baseDTO.getContent(), baseDTO.getNickname(),
                baseDTO.getPostCategory(), baseDTO.getUserImageUrl(), baseDTO.getPostImageUrl(), baseDTO.isAnonymous(),
                baseDTO.getLikeCount(), baseDTO.getScrapCount(), baseDTO.getHits(), baseDTO.getCreatedAt(),
                baseDTO.getCommentCount(), baseDTO.getUserInfo());
        this.reportInfo = reportInfo;
    }


    public static ReportListResponseDto  from(PostDetailResponseDTO base, ReportInfo reportInfo) {
        return ReportListResponseDto.builder()
                .baseDTO(base)
                .reportInfo(reportInfo)
                .build();
    }
}