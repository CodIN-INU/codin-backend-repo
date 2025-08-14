package inu.codin.codin.domain.lecture.service;

import inu.codin.codin.domain.elasticsearch.indexer.LectureStartupIndexer;
import inu.codin.codin.domain.lecture.exception.LectureErrorCode;
import inu.codin.codin.domain.lecture.exception.LectureUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class LectureUploadService {

    @Value("${lecture.file.path}")
    private String UPLOAD_DIR;

    @Value("${lecture.python.path}")
    private String PYTHON_DIR;

    private LectureStartupIndexer indexer;

    private String ROOM_PROGRAM = "dayTimeOfRoom.py";
    private String LECTURE_PROGRAM = "infoOfLecture.py";

    public void uploadNewSemesterLectures(MultipartFile file){
        try {
            saveFile(file);
            int exitCode = executeGetExitCode(file, LECTURE_PROGRAM);

            if (exitCode != 0) {
                log.error("[uploadNewSemesterLectures] {} 업로드 실패", file.getOriginalFilename());

                throw new LectureUploadException(LectureErrorCode.LECTURE_ROOM_UPLOAD_FAIL, exitCode);
            }
            log.info("[uploadNewSemesterLectures] {} 학기 강의 정보 업로드 완료", file.getName());

            indexer.lectureIndex();
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e.getStackTrace()[0]);

            throw new LectureUploadException(LectureErrorCode.LECTURE_ROOM_UPLOAD_FAIL, e.getMessage());
        }
    }

    public void uploadNewSemesterRooms(MultipartFile file) {
        try {
            saveFile(file);
            int exitCode = executeGetExitCode(file, ROOM_PROGRAM);

            if (exitCode != 0) {
                log.error("[uploadNewSemesterRooms] {} 강의실 현황 업데이트 실패", file.getOriginalFilename());

                throw new LectureUploadException(LectureErrorCode.LECTURE_UPLOAD_FAIL, exitCode);
            }

            log.info("[uploadNewSemesterRooms] {} 강의실 현황 업데이트 완료", file.getName());
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e.getStackTrace()[0]);

            throw new LectureUploadException(LectureErrorCode.LECTURE_UPLOAD_FAIL, e.getMessage());
        }
    }

    private void saveFile(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new LectureUploadException(LectureErrorCode.FILE_READ_FAIL, "파일 이름이 비어있습니다.");
        }

        File savedFile = new File(UPLOAD_DIR + file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(savedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            throw new LectureUploadException(LectureErrorCode.FILE_READ_FAIL, e.getMessage());
        }
    }

    private int executeGetExitCode(MultipartFile file, String pythonNm) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                PYTHON_DIR, UPLOAD_DIR + pythonNm, UPLOAD_DIR+ file.getOriginalFilename()
        );
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("[Python Output] {}", line);
            }
        }

        return process.waitFor();
    }
}
