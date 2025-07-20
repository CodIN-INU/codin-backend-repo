package inu.codin.codinlectureapi.global.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ListResponse<T> extends CommonResponse{
    List<T> dataList;

    @Builder
    public ListResponse(int code, String message, List<T> dataList) {
        super(true, code, message);
        this.dataList = dataList;
    }
}
