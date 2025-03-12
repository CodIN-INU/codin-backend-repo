package inu.codin.codin.domain.report.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.report.dto.ReportInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;@Getter

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