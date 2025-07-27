package inu.codin.codin.domain.lecture.service;

import inu.codin.codin.domain.lecture.entity.Semester;
import inu.codin.codin.domain.lecture.exception.SemesterErrorCode;
import inu.codin.codin.domain.lecture.exception.SemesterException;
import inu.codin.codin.domain.lecture.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository semesterRepository;

    public Optional<Semester> getSemester(String semester) {
        try {
            int [] parsed = parseSemester(semester);
            return semesterRepository.findSemesterByYearAndQuarter(parsed[0], parsed[1]);
        } catch (Exception e) {
            throw new SemesterException(SemesterErrorCode.SEMESTER_INVALID_FORMAT);
        }
    }

    public int[] parseSemester(String semesterStr) {
        String[] parts = semesterStr.split("-");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }
}
