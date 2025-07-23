package inu.codin.codin.domain.lecture.repository;

import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.global.common.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {

    @Query("SELECT l FROM lectures l JOIN FETCH l.schedule WHERE l.lectureCode = :lectureCode")
    Optional<Lecture> findLectureAndScheduleByLectureCode(String lectureCode);
    Page<Lecture> findAllByDepartment(Pageable pageable, Department department);

}
