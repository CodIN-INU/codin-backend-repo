//package inu.codin.codin.domain.report.dto.response;
//
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class ReportPageResponse {
//
//    private List<ReportListResponseDto> contents = new ArrayList<>();
//    private long lastPage;
//    private long nextPage;
//
//    private ReportPageResponse(List<ReportListResponseDto> contents, long lastPage, long nextPage) {
//        this.contents = contents;
//        this.lastPage = lastPage;
//        this.nextPage = nextPage;
//    }
//
//    public static ReportPageResponse of(List<ReportListResponseDto> reportPaging, long totalElements, long nextPage) {
//        return ReportPageResponse.newPagingHasNext(reportPaging, totalElements, nextPage);
//    }
//
//    private static ReportPageResponse newPagingHasNext(List<ReportListResponseDto> reports, long totalElements, long nextPage) {
//        return new ReportPageResponse(reports, totalElements, nextPage);
//    }
//
//}