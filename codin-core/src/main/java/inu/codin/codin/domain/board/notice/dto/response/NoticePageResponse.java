package inu.codin.codin.domain.board.notice.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticePageResponse {

    private List<NoticeDetailResponseDto> contents = new ArrayList<>();
    private long lastPage;
    private long nextPage;

    private NoticePageResponse(List<NoticeDetailResponseDto> contents, long lastPage, long nextPage) {
        this.contents = contents;
        this.lastPage = lastPage;
        this.nextPage = nextPage;
    }

    public static NoticePageResponse of(List<NoticeDetailResponseDto> postPaging, long totalElements, long nextPage) {
        return NoticePageResponse.newPagingHasNext(postPaging, totalElements, nextPage);
    }

    private static NoticePageResponse newPagingHasNext(List<NoticeDetailResponseDto> posts, long totalElements, long nextPage) {
        return new NoticePageResponse(posts, totalElements, nextPage);
    }

}