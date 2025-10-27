package inu.codin.codin.domain.post.dto.response;

import lombok.Getter;
import lombok.Builder;


@Getter
public class PostPollDetailResponseDTO {
    private final PostDetailResponseDTO post;
    private final PollInfoResponseDTO poll;

    @Builder
    public PostPollDetailResponseDTO(PostDetailResponseDTO post, PollInfoResponseDTO poll) {
        this.post = post;
        this.poll = poll;
    }

    public static PostPollDetailResponseDTO of(PostDetailResponseDTO post, PollInfoResponseDTO poll) {
        return PostPollDetailResponseDTO.builder()
                .post(post)
                .poll(poll)
                .build();
    }
}