package inu.codin.codin.domain.lecture.repository;

import inu.codin.codin.domain.elasticsearch.document.LectureDocument;
import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.entity.Semester;
import inu.codin.codin.domain.lecture.entity.SortingOption;
import inu.codin.codin.global.common.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LectureSearchRepositoryCustom {
    Page<LectureDocument> searchLecturesAtPreview(String keyword, Department department, SortingOption sortingOption, List<String> likeIdList, Pageable pageable, Boolean like);

    List<Lecture> searchLecturesAtReview(Department department, Integer grade, Semester semester);
}
