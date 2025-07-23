package inu.codin.codin.domain.review.repository;

import inu.codin.codin.domain.lecture.entity.Emotion;
import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUserIdAndLectureAndDeletedAtIsNull(Long userId, Lecture lecture);

    @Query("""
        SELECT AVG(r.starRating) FROM reviews r WHERE r.lecture =:lecture
    """)
    Double getAvgOfStarRatingByLecture(Lecture lecture);


    @Query(nativeQuery = true,
        value = """
            SELECT
                SUM(CASE WHEN r.star_rating BETWEEN 0.25 AND 1.5 THEN 1 ELSE 0 END) AS hard,
                SUM(CASE WHEN r.star_rating > 1.75 AND r.star_rating <= 3.5 THEN 1 ELSE 0 END) AS ok,
                SUM(CASE WHEN r.star_rating > 3.75 AND r.star_rating <= 5.0 THEN 1 ELSE 0 END) AS best
            FROM
                reviews r
            WHERE
                lecture_id = :lectureId;
    """)
    Emotion getEmotionsCountByRange(Long lectureId);

    Page<Review> findReviewsByLecture(Lecture lecture, Pageable pageable);
}
