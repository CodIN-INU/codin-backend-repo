package inu.codin.codin.domain.lecture.controller;

import inu.codin.codin.domain.lecture.dto.MetaMode;
import inu.codin.codin.domain.lecture.service.LectureUploadService;
import inu.codin.codin.global.common.response.SingleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@Tag(name = "Lecture Upload API", description = "강의 내역 및 강의실 현황 업데이트 API")
public class LectureUploadController {

    private final LectureUploadService lectureUploadService;

    @Operation(
            summary = "새 학기의 강의 내역 엑셀파일 업로드",
            description = "강의 내역서(엑셀 파일) 이름을 '년도-학기'로 설정하여 업로드 ex) 24-1.xlsx, 24-2.xlsx"
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public ResponseEntity<SingleResponse<?>> uploadNewSemesterLectures(@RequestParam("excelFile") MultipartFile file) {
        lectureUploadService.uploadNewSemesterLectures(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, file.getOriginalFilename()+"의 강의 내역 업로드", null));

    }

    @Operation(
            summary = "강의실 현황 엑셀파일 업데이트",
            description = "강의 내역서(엑셀 파일) 이름을 '년도-학기'로 설정하여 업로드 ex) 24-1.xlsx, 24-2.xlsx"
    )
    @PostMapping(value = "/rooms", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public ResponseEntity<SingleResponse<?>> uploadNewSemesterRooms(@RequestParam("excelFile") MultipartFile file) {
        lectureUploadService.uploadNewSemesterRooms(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, file.getOriginalFilename()+"의 강의실 현황 업데이트", null));

    }

    @Operation(
            summary = "강의 메타(키워드/태그/선수과목) 엑셀 업로드",
            description = "'단과대약어_연도_학기_meta'로 설정하여 업로드 ex) info_25_2_meta.xlxs. 기본값은 모두 true (즉 전체 처리)."
    )
    @PostMapping(value = "/meta", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public ResponseEntity<SingleResponse<?>> uploadLectureMeta(
            @Parameter(description = "업로드할 엑셀 파일 (.xlsx)")
            @RequestParam("excelFile") MultipartFile file,

            @Parameter(description = "처리 모드 (ALL | KEYWORDS | TAGS | PRE_COURSES). 기본값: ALL")
            @RequestParam(name = "mode", defaultValue = "ALL") MetaMode mode
    ) {
        lectureUploadService.uploadLectureMeta(file, mode);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201,
                        file.getOriginalFilename()+"의 태그,키워드,선수 과목등 메타데이터 포함 엑셀파일 업데이트",
                        null));

    }


}
