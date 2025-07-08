package inu.codin.codin.domain.post.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PostPageItemResponseDTO {
    private final PostDetailResponseDTO post;
    private final PollInfoResponseDTO poll;

    @Builder
    public PostPageItemResponseDTO(PostDetailResponseDTO post, PollInfoResponseDTO poll) {
        this.post = post;
        this.poll = poll;
    }

    public static PostPageItemResponseDTO of(PostDetailResponseDTO post, PollInfoResponseDTO poll) {
        return PostPageItemResponseDTO.builder()
                .post(post)
                .poll(poll)
                .build();
    }
} 