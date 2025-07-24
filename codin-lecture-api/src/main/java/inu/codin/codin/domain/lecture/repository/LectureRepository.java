package inu.codin.codin.domain.lecture.repository;

import inu.codin.codin.domain.lecture.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    @Query("SELECT l FROM Lecture l LEFT JOIN FETCH l.schedule LEFT JOIN FETCH l.tags WHERE l.id = :lectureId")
    Optional<Lecture> findLectureWithScheduleAndTagsById(Long lectureId);

    @Query("SELECT l FROM Lecture l JOIN FETCH l.semester JOIN FETCH l.reviews WHERE l.id = :lectureId")
    Optional<Lecture> findLectureWithSemesterAndReviewsById(Long lectureId);
}
