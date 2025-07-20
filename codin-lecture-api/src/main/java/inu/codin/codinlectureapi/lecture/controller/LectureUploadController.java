package inu.codin.codinlectureapi.lecture.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.lecture.service.LectureUploadService;
import io.swagger.v3.oas.annotations.Operation;
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
            summary = "새 학기의 강의 내역 업로드",
            description = "강의 내역서(엑셀 파일) 이름을 '년도-학기'로 설정하여 업로드 ex) 24-1.xlsx, 24-2.xlsx"
    )
    @PostMapping(value = "/lectures", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public ResponseEntity<SingleResponse<?>> uploadNewSemesterLectures(@RequestParam("excelFile") MultipartFile file) {
        lectureUploadService.uploadNewSemesterLectures(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, file.getOriginalFilename()+"의 강의 내역 업로드", null));

    }

    @Operation(
            summary = "강의실 현황 업데이트",
            description = "강의 내역서(엑셀 파일) 이름을 '년도-학기'로 설정하여 업로드 ex) 24-1.xlsx, 24-2.xlsx"
    )
    @PostMapping(value = "/rooms", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public ResponseEntity<?> uploadNewSemesterRooms(@RequestParam("excelFile") MultipartFile file) {
        lectureUploadService.uploadNewSemesterRooms(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, file.getOriginalFilename()+"의 강의실 현황 업데이트", null));

    }


}
