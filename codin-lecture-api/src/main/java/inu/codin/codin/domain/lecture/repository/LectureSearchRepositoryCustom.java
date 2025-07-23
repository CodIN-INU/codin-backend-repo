package inu.codin.codin.domain.lecture.repository;

import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.entity.SortingOption;
import inu.codin.codin.global.common.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

public interface LectureSearchRepositoryCustom {
    Page<Lecture> search(String keyword, Department department, SortingOption sortingOption, Pageable pageable);
}
