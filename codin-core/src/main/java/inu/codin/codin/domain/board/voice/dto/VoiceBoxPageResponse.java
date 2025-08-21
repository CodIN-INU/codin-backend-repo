package inu.codin.codin.domain.board.voice.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoiceBoxPageResponse {

    private List<VoiceBoxDetailResponse> contents = new ArrayList<>();
    private long lastPage;
    private long nextPage;

    private VoiceBoxPageResponse(List<VoiceBoxDetailResponse> contents, long lastPage, long nextPage) {
        this.contents = contents;
        this.lastPage = lastPage;
        this.nextPage = nextPage;
    }

    public static VoiceBoxPageResponse of(List<VoiceBoxDetailResponse> postPaging, long lastPage, long nextPage) {
        return VoiceBoxPageResponse.newPagingHasNext(postPaging, lastPage, nextPage);
    }

    private static VoiceBoxPageResponse newPagingHasNext(List<VoiceBoxDetailResponse> posts, long lastPage, long nextPage) {
        return new VoiceBoxPageResponse(posts, lastPage, nextPage);
    }

}
