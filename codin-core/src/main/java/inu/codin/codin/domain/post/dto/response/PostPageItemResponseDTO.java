package inu.codin.codin.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostPageItemResponseDTO {
    @JsonUnwrapped // 추후 프론트에 얘기해서 {post:{}, poll:{} 구조로 변경} 현재:{,,,poll:{}}
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