package inu.codin.codin.domain.post.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostPageResponse {

    private List<PostListResponseDto> contents = new ArrayList<>();
    private long lastPage;
    private long nextPage;

    private PostPageResponse(List<PostListResponseDto> contents, long lastPage, long nextPage) {
        this.contents = contents;
        this.lastPage = lastPage;
        this.nextPage = nextPage;
    }

    public static PostPageResponse of(List<PostListResponseDto> postPaging, long totalElements, long nextCursor) {
        return PostPageResponse.newPagingHasNext(postPaging, totalElements, nextCursor);
    }

    private static PostPageResponse newPagingHasNext(List<PostListResponseDto> posts, long totalElements, long nextCursor) {
        return new PostPageResponse(posts, totalElements, nextCursor);
    }

}