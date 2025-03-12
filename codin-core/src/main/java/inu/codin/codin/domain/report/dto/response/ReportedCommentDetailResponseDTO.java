package inu.codin.codin.domain.report.dto.response;

import inu.codin.codin.domain.post.domain.comment.dto.response.CommentResponseDTO;
import lombok.Getter;

@Getter
public class ReportedCommentDetailResponseDTO extends CommentResponseDTO {
    private final boolean isReported;

    private ReportedCommentDetailResponseDTO(CommentResponseDTO base, boolean isReported) {
        super(base.get_id(), base.getUserId(), base.getContent(), base.getNickname(),
                base.getUserImageUrl(), base.isAnonymous(), base.getReplies(),
                base.getLikeCount(), base.isDeleted(), base.getCreatedAt(), base.getUserInfo());
        this.isReported = isReported;
    }

    public static ReportedCommentDetailResponseDTO from(CommentResponseDTO base, boolean isReported) {
        return new ReportedCommentDetailResponseDTO(base, isReported);
    }
}
