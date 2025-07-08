package inu.codin.codin.domain.post.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PostPageResponse {

    private List<PostDetailResponseDTO> contents = new ArrayList<>();
    private long lastPage;
    private long nextPage;

    @Builder
    private PostPageResponse(List<PostDetailResponseDTO> contents, long lastPage, long nextPage) {
        this.contents = contents;
        this.lastPage = lastPage;
        this.nextPage = nextPage;
    }

    public static PostPageResponse of(List<PostDetailResponseDTO> postPaging, long totalElements, long nextPage) {
        return PostPageResponse.builder()
                .contents(postPaging)
                .lastPage(totalElements)
                .nextPage(nextPage)
                .build();
    }

}