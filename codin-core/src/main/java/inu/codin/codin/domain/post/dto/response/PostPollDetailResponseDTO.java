package inu.codin.codin.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import inu.codin.codin.domain.post.dto.response.PollInfoResponseDTO;

@Getter
public class PostPollDetailResponseDTO extends PostDetailResponseDTO {

    private final PollInfoResponseDTO poll;

    @Builder
    private PostPollDetailResponseDTO(PostDetailResponseDTO baseDTO, PollInfoResponseDTO poll) {
        super(baseDTO.getUserId(), baseDTO.get_id(), baseDTO.getTitle(), baseDTO.getContent(), baseDTO.getNickname(),
                baseDTO.getPostCategory(), baseDTO.getUserImageUrl(), baseDTO.getPostImageUrl(), baseDTO.isAnonymous(), baseDTO.getLikeCount(),
                baseDTO.getScrapCount(), baseDTO.getHits(), baseDTO.getCreatedAt(), baseDTO.getCommentCount(), baseDTO.getUserInfo());
        this.poll = poll;
    }

    public static PostPollDetailResponseDTO of(PostDetailResponseDTO base, PollInfoResponseDTO poll) {
        return PostPollDetailResponseDTO.builder()
                .baseDTO(base)
                .poll(poll)
                .build();
    }
}