package inu.codin.codinlectureapi.lecture.service;

import inu.codin.codin.domain.lecture.exception.LecturePythonException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class LectureUploadService {

    @Value("${lecture.file.path}")
    private String UPLOAD_DIR;

    @Value("${lecture.python.path}")
    private String PYTHON_DIR;

    public void uploadNewSemesterRooms(MultipartFile file) {
        try {
            saveFile(file);
            String pythonNm = "dayTimeOfRoom.py";
            ProcessBuilder processBuilder = new ProcessBuilder(
                    PYTHON_DIR, UPLOAD_DIR + pythonNm, UPLOAD_DIR+file.getOriginalFilename()
            );
            log.info(processBuilder.command().toString());
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            String result = new String(process.getInputStream().readAllBytes());
            log.info(result);
            log.warn("[uploadNewSemesterRooms] Exited dayTimeOfRoom.py with error code" + exitCode);
            if (exitCode == 0)
                log.info("[uploadNewSemesterRooms] {} 강의실 현황 업데이트 완료", file.getName());
            else {
                log.error("[uploadNewSemesterRooms] {} 강의실 현황 업데이트 실패", file.getOriginalFilename());
                throw new LecturePythonException("강의실 현황 업데이트 실패, Python errorCode : " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e.getStackTrace()[0]);
            throw new LecturePythonException(e.getMessage());
        }
    }

    public void uploadNewSemesterLectures(MultipartFile file){
        try {
            String pythonNm = "infoOfLecture.py";
            ProcessBuilder processBuilder = new ProcessBuilder(
                    PYTHON_DIR, UPLOAD_DIR + pythonNm, UPLOAD_DIR+file.getOriginalFilename()
            );
            log.info(processBuilder.command().toString());
            Process process = processBuilder.start();

            String result = new String(process.getInputStream().readAllBytes());
            log.info(result);
            int exitCode = process.waitFor();
            log.warn("[uploadNewSemesterLectures] Exited infoOfLecture.py with error code " + exitCode);
            if (exitCode == 0)
                log.info("[uploadNewSemesterLectures] {} 학기 강의 정보 업로드 완료", file.getName());
            else {
                log.error("[uploadNewSemesterLectures] {} 업로드 실패", file.getOriginalFilename());
                throw new LecturePythonException("강의실 현황 업데이트 실패, Python errorCode : " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e.getStackTrace()[0]);
            throw new LecturePythonException(e.getMessage());
        }
    }

    private void saveFile(MultipartFile file) {
        File savedFile = new File(UPLOAD_DIR + file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(savedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
