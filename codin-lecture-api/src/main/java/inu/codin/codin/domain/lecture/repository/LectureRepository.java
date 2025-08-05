package inu.codin.codin.domain.lecture.repository;

import inu.codin.codin.domain.lecture.entity.Lecture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    @Query("""
        SELECT l FROM Lecture l
        LEFT JOIN FETCH l.schedule
        LEFT JOIN FETCH l.tags
        WHERE l.id=:lectureId
    """)
    Optional<Lecture> findLectureWithScheduleAndTagsById(Long lectureId);

    @Query("""
        SELECT l FROM Lecture l
        LEFT JOIN FETCH l.semester
        LEFT JOIN FETCH l.reviews
        WHERE l.id=:lectureId
    """)
    Optional<Lecture> findLectureWithSemesterAndReviewsById(Long lectureId);

    @Query("""
      SELECT DISTINCT l FROM Lecture l
      LEFT JOIN FETCH l.tags
      LEFT JOIN FETCH l.semester
      LEFT JOIN FETCH l.schedule
    """)
    Page<Lecture> findAllWithAssociations(Pageable pageable);


    @Query("""
      SELECT DISTINCT l FROM Lecture l
      LEFT JOIN FETCH l.tags
      LEFT JOIN FETCH l.reviews
      WHERE l.id=:lectureId
    """)
    Optional<Lecture> findLectureWithTagsAndReviewsById(Long lectureId);
}
