package inu.codin.codin.domain.scrap.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.scrap.entity.ScrapEntity;
import inu.codin.codin.domain.scrap.repository.ScrapRepository;
import inu.codin.codin.infra.redis.service.RedisBestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapService {
    private final ScrapRepository scrapRepository;
    private final PostRepository postRepository;

    private final RedisBestService redisBestService;

    /**
     * 하나의 모듈로 스크랩 추가, 삭제 toggle 동작
     * @param id 게시글 _id (postId)
     * @return 스크랩 상태의 message 반환 (스크랩 추가, 복원, 삭제)
     */
    public String toggleScrap(String id) {
        log.info("스크랩 토글 요청 - postId: {}", id);
        ObjectId postId = new ObjectId(id);
        ObjectId userId = SecurityUtils.getCurrentUserId();

        postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> {
                    log.error("스크랩 토글 실패 - 게시글을 찾을 수 없음 - postId: {}", postId);
                    return new NotFoundException("[toggleScrap] 게시글을 찾을 수 없습니다.");
                });

        // 이미 스크랩한 게시물인지 확인
        Optional<ScrapEntity> scrap =  scrapRepository.findByPostIdAndUserId(postId, userId);
        return getResult(scrap, postId, userId);

    }

    /**
     * 스크랩이 존재하는 경우
     * 1. 삭제된 스크랩 -> 복원
     * 2. 스크랩 중 -> 삭제
     *
     * 스크랩이 존재하지 않는 경우
     * 1. 스크랩 생성
     */
    private String getResult(Optional<ScrapEntity> scrap, ObjectId postId, ObjectId userId) {
        if (scrap.isPresent()) {
            if (scrap.get().getDeletedAt() == null){ //스크랩 존재 -> 삭제
                removeScrap(scrap.get());
                return "스크랩이 취소되었습니다.";
            } else { //삭제된 스크랩 존재 -> 복구
                restoreScrap(scrap.get());
                return "스트랩이 추가(복구)되었습니다.";
            }
        }
        else addScrap(postId, userId); //첫 스크랩
        return "스크랩이 추가되었습니다.";
    }

    private void restoreScrap(ScrapEntity scrap) {
        scrap.recreatedAt();
        scrap.restore();
        scrapRepository.save(scrap);
        log.info("스크랩 복원 완료 - postId: {}, userId: {}", scrap.getPostId(), scrap.getUserId());

    }

    private void addScrap(ObjectId postId, ObjectId userId) {
        scrapRepository.save(ScrapEntity.builder()
                .postId(postId)
                .userId(userId)
                .build());
        redisBestService.applyBestScore(2, postId); //Best 게시글에 적용
        log.info("스크랩 추가 완료 - postId: {}, userId: {}", postId, userId);
        log.info("Redis에 Best Score 적용 - postId: {}", postId);

    }

    private void removeScrap(ScrapEntity scrap) {
        scrap.delete();
        scrapRepository.save(scrap);
        log.info("스크랩 삭제 완료 - postId: {}, userId: {}", scrap.getPostId(), scrap.getUserId());
    }

    public int getScrapCount(ObjectId postId) {
        return scrapRepository.countByPostIdAndDeletedAtIsNull(postId);
    }

    public boolean isPostScraped(ObjectId postId, ObjectId userId){
        return scrapRepository.existsByPostIdAndUserId(postId, userId);
    }
}