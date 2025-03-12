package inu.codin.codin.domain.report.dto.response;

import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReportedPostDetailResponseDTO extends PostDetailResponseDTO {
    private final boolean isReported;

    @Builder
    public ReportedPostDetailResponseDTO(PostDetailResponseDTO baseDTO, boolean isReported) {
        super(baseDTO.getUserId(), baseDTO.get_id(), baseDTO.getTitle(), baseDTO.getContent(), baseDTO.getNickname(),
                baseDTO.getPostCategory(), baseDTO.getUserImageUrl(), baseDTO.getPostImageUrl(), baseDTO.isAnonymous(),
                baseDTO.getLikeCount(), baseDTO.getScrapCount(), baseDTO.getHits(), baseDTO.getCreatedAt(),
                baseDTO.getCommentCount(), baseDTO.getUserInfo());
        this.isReported = isReported;
    }

    public static ReportedPostDetailResponseDTO from(boolean isReported, PostDetailResponseDTO postDetail) {
        return ReportedPostDetailResponseDTO.builder()
                .isReported(isReported)
                .baseDTO(postDetail)
                .build();
    }
}
