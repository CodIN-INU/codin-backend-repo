package inu.codin.lecture.domain.review.repository;

import inu.codin.lecture.domain.review.entity.UserReviewStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserReviewStatsRepository extends JpaRepository<UserReviewStats, Long> {
    Optional<UserReviewStats> findByUserId(String userId);
}
