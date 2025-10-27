package inu.codin.codin.domain.report.dto.response;

import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReportedPostDetailResponseDTO {
    private final PostDetailResponseDTO post;
    private final boolean isReported;

    @Builder
    public ReportedPostDetailResponseDTO(PostDetailResponseDTO post, boolean isReported) {
        this.post = post;
        this.isReported = isReported;
    }

    public static ReportedPostDetailResponseDTO from(PostDetailResponseDTO post, boolean isReported) {
        return ReportedPostDetailResponseDTO.builder()
                .post(post)
                .isReported(isReported)
                .build();
    }
}
