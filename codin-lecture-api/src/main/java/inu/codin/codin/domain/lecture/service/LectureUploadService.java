package inu.codin.codin.domain.lecture.service;

import inu.codin.codin.domain.elasticsearch.indexer.LectureStartupIndexer;
import inu.codin.codin.domain.lecture.dto.MetaMode;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LectureUploadService {

    @Value("${lecture.file.path}")
    private String UPLOAD_DIR;

    @Value("${lecture.python.path}")
    private String PYTHON_DIR;

    private final LectureStartupIndexer indexer;

    private String ROOM_PROGRAM = "dayTimeOfRoom.py";
    private String LECTURE_PROGRAM = "infoOfLecture.py";
    private static final String META_PROGRAM = "load_metadata.py";


    public void uploadNewSemesterLectures(MultipartFile file){
        try {
            saveFile(file);
            int exitCode = executeGetExitCode(file, LECTURE_PROGRAM);

            if (exitCode != 0) {
                log.error("[uploadNewSemesterLectures] {} 업로드 실패", file.getOriginalFilename());

                throw new LectureUploadException(LectureErrorCode.LECTURE_UPLOAD_FAIL, exitCode);
            }
            log.info("[uploadNewSemesterLectures] {} 학기 강의 정보 업로드 완료", file.getName());

            indexer.lectureIndex();
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);

            throw new LectureUploadException(LectureErrorCode.LECTURE_UPLOAD_FAIL, e.getMessage());
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

    public void uploadLectureMeta(MultipartFile file, MetaMode mode) {
        try {
            saveFile(file); // 기존 그대로 사용
            executeMetaLoader(file, mode);
            log.info("[uploadLectureMeta] {} 메타 업데이트 완료", file.getOriginalFilename());

            try { indexer.lectureIndex(); } catch (Exception e) {
                log.warn("[uploadLectureMeta] 색인 갱신 경고: {}", e.getMessage());
            }

        } catch (LectureUploadException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new LectureUploadException(LectureErrorCode.LECTURE_UPLOAD_FAIL, e.getMessage());
        }
    }


    /** 메타 로더 실행 헬퍼: load_metadata.py <excel> [--keywords] [--tags] [--pre-courses] | --all */
    private void executeMetaLoader(MultipartFile file,MetaMode mode) {
        String scriptPath = Paths.get(UPLOAD_DIR, META_PROGRAM).toString();
        String excelPath  = Paths.get(UPLOAD_DIR, file.getOriginalFilename()).toString();

        List<String> cmd = new ArrayList<>();
        cmd.add(PYTHON_DIR);                                 // ex) /usr/bin/python3 or venv/bin/python
        cmd.add(scriptPath); // load_metadata.py 절대/상대 경로
        cmd.add(excelPath); // 업로드된 엑셀 파일 경로

        switch (mode) {
            case ALL -> cmd.add("--all");
            case KEYWORDS -> cmd.add("--keywords");
            case TAGS -> cmd.add("--tags");
            case PRE_COURSES -> cmd.add("--pre-courses");
        }
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        try {
            Process p = pb.start();
            StringBuilder out = new StringBuilder();
            try (var br = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    out.append(line).append('\n');
                    log.debug("[Python Output] {}", line);
                }
            }
            int exit = p.waitFor();
            if (exit != 0) {
                throw new LectureUploadException(LectureErrorCode.LECTURE_UPLOAD_FAIL, out.toString());
            }
        } catch (IOException | InterruptedException e) {
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
