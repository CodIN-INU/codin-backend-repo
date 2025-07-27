package inu.codin.codin.domain.lecture.repository;

import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.entity.Semester;
import inu.codin.codin.domain.lecture.entity.SortingOption;
import inu.codin.codin.global.common.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LectureSearchRepositoryCustom {
    Page<Lecture> searchLecturesAtPreview(String keyword, Department department, SortingOption sortingOption, Boolean like, Pageable pageable);

    List<Lecture> searchLecturesAtReview(Department department, Integer grade, Semester semester);
}
