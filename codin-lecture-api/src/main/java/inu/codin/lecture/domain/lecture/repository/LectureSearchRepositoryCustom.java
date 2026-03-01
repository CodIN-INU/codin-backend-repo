package inu.codin.lecture.domain.lecture.repository;

import inu.codin.lecture.domain.elasticsearch.document.LectureDocument;
import inu.codin.lecture.domain.lecture.entity.Lecture;
import inu.codin.lecture.domain.lecture.entity.Semester;
import inu.codin.lecture.domain.lecture.entity.SortingOption;
import inu.codin.common.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LectureSearchRepositoryCustom {
    Page<LectureDocument> searchLectureList(String keyword, Department department, SortingOption sortingOption, List<String> likeIdList, Pageable pageable, Boolean like);

    List<Lecture> searchLecturesAtReview(Department department, Integer grade, Semester semester);
}
