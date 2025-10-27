package inu.codin.codin.domain.lecture.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class
LecturePageResponse {

    private List<LecturePreviewResponseDto> contents = new ArrayList<>();
    private long lastPage;
    private long nextPage;

    public LecturePageResponse(List<LecturePreviewResponseDto> contents, long lastPage, long nextPage) {
        this.contents = contents;
        this.lastPage = lastPage;
        this.nextPage = nextPage;
    }

    public static LecturePageResponse of(List<LecturePreviewResponseDto> lecturePaging, long totalElements, long nextPage){
        return LecturePageResponse.nextPagingHasNext(lecturePaging, totalElements, nextPage);
    }

    public static LecturePageResponse nextPagingHasNext(List<LecturePreviewResponseDto> lectures, long totalElements, long nextPage){
        return new LecturePageResponse(lectures, totalElements, nextPage);
    }
}
