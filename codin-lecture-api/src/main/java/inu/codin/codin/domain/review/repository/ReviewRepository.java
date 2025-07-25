package inu.codin.codin.domain.review.repository;

import inu.codin.codin.domain.lecture.entity.Emotion;
import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUserIdAndLectureAndDeletedAtIsNull(String userId, Lecture lecture);

    @Query("""
        SELECT AVG(r.starRating) FROM Review r WHERE r.lecture =:lecture
    """)
    Double getAvgOfStarRatingByLecture(Lecture lecture);

    Page<Review> findReviewsByLecture(Lecture lecture, Pageable pageable);
}
