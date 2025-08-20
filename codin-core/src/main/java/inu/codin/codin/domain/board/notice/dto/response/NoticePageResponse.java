package inu.codin.codin.domain.board.notice.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticePageResponse {

    private List<NoticeListResponseDto> contents = new ArrayList<>();
    private long lastPage;
    private long nextPage;

    private NoticePageResponse(List<NoticeListResponseDto> contents, long lastPage, long nextPage) {
        this.contents = contents;
        this.lastPage = lastPage;
        this.nextPage = nextPage;
    }

    public static NoticePageResponse of(List<NoticeListResponseDto> postPaging, long totalElements, long nextPage) {
        return NoticePageResponse.newPagingHasNext(postPaging, totalElements, nextPage);
    }

    private static NoticePageResponse newPagingHasNext(List<NoticeListResponseDto> posts, long totalElements, long nextPage) {
        return new NoticePageResponse(posts, totalElements, nextPage);
    }

}