package inu.codin.codin.domain.review.service;

import inu.codin.codin.domain.review.entity.UserReviewStats;
import inu.codin.codin.domain.review.repository.UserReviewStatsRepository;
import inu.codin.codin.global.auth.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserReviewStatsService {

    private final UserReviewStatsRepository userReviewStatsRepository;

    public void updateStats(String userId) {
        UserReviewStats userReviewStats = userReviewStatsRepository.findByUserId(userId)
                        .orElse(new UserReviewStats(userId));
        userReviewStats.increaseCount();
        if (!userReviewStats.isOpenKeyword() && userReviewStats.getCountOfReviews() >= 3)
            userReviewStats.canOpenKeyword();
        userReviewStatsRepository.save(userReviewStats);
    }

    public boolean isOpenKeyword() {
        String userId = SecurityUtils.getUserId();
        UserReviewStats userReviewStats = userReviewStatsRepository.findByUserId(userId)
                        .orElse(new UserReviewStats(userId));
        return userReviewStats.isOpenKeyword();
    }
}
