package inu.codin.codin.domain.review.service;

import inu.codin.codin.domain.review.entity.UserReviewStats;
import inu.codin.codin.domain.review.repository.UserReviewStatsRepository;
import inu.codin.codin.global.auth.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserReviewStatsService {

    private static final int KEYWORD_UNLOCK_THRESHOLD = 3;
    private final UserReviewStatsRepository userReviewStatsRepository;

    public void updateStats(String userId) {
        UserReviewStats userReviewStats = userReviewStatsRepository.findByUserId(userId)
                        .orElseGet(() -> new UserReviewStats(userId));
        userReviewStats.increaseCount();
        if (!userReviewStats.isOpenKeyword() && userReviewStats.getCountOfReviews() >= KEYWORD_UNLOCK_THRESHOLD)
            userReviewStats.canOpenKeyword();
        userReviewStatsRepository.save(userReviewStats);
    }

    public boolean isOpenKeyword() {
        String userId = SecurityUtils.getUserId();
        return userReviewStatsRepository.findByUserId(userId)
                        .map(UserReviewStats::isOpenKeyword)
                        .orElse(false);
    }
}
