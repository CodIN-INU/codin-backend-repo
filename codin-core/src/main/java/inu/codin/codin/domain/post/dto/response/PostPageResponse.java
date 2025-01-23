package inu.codin.codin.domain.post.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostPageResponse {

    private List<PostDetailResponseDTO> contents = new ArrayList<>();
    private long lastPage;
    private long nextPage;

    private PostPageResponse(List<PostDetailResponseDTO> contents, long lastPage, long nextPage) {
        this.contents = contents;
        this.lastPage = lastPage;
        this.nextPage = nextPage;
    }

    public static PostPageResponse of(List<PostDetailResponseDTO> postPaging, long totalElements, long nextPage) {
        return PostPageResponse.newPagingHasNext(postPaging, totalElements, nextPage);
    }

    private static PostPageResponse newPagingHasNext(List<PostDetailResponseDTO> posts, long totalElements, long nextPage) {
        return new PostPageResponse(posts, totalElements, nextPage);
    }

}