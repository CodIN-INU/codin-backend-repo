package inu.codin.codin.domain.review.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewPageResponse {

    private List<ReviewListResposneDto> contents = new ArrayList<>();
    private long lastPage;
    private long nextPage;

    public ReviewPageResponse(List<ReviewListResposneDto> contents, long lastPage, long nextPage) {
        this.contents = contents;
        this.lastPage = lastPage;
        this.nextPage = nextPage;
    }

    public static ReviewPageResponse of(List<ReviewListResposneDto> reviewPaging, long totalElements, long nextPage){
        return ReviewPageResponse.nextPagingHasNext(reviewPaging, totalElements, nextPage);
    }

    public static ReviewPageResponse nextPagingHasNext(List<ReviewListResposneDto> reviews, long totalElements, long nextPage){
        return new ReviewPageResponse(reviews, totalElements, nextPage);
    }
}
