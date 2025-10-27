package inu.codin.codin.domain.lecture.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LecturePageResponse {

    private List<LectureListResponseDto> contents = new ArrayList<>();
    private long lastPage;
    private long nextPage;

    public LecturePageResponse(List<LectureListResponseDto> contents, long lastPage, long nextPage) {
        this.contents = contents;
        this.lastPage = lastPage;
        this.nextPage = nextPage;
    }

    public static LecturePageResponse of(List<LectureListResponseDto> lecturePaging, long totalElements, long nextPage){
        return LecturePageResponse.nextPagingHasNext(lecturePaging, totalElements, nextPage);
    }

    public static LecturePageResponse nextPagingHasNext(List<LectureListResponseDto> lectures, long totalElements, long nextPage){
        return new LecturePageResponse(lectures, totalElements, nextPage);
    }
}
