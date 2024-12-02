package inu.codin.codin.domain.post.like;

import inu.codin.codin.domain.post.like.entity.LikeEntity;
import inu.codin.codin.domain.post.like.entity.LikeType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface LikeRepository extends MongoRepository<LikeEntity, String> {
    // 특정 엔티티(게시글/댓글/대댓글)의 좋아요 개수 조회
    long countByEntityTypeAndEntityId(LikeType entityType, String entityId);

    // 특정 엔티티의 좋아요 데이터 조회
    List<LikeEntity> findByEntityTypeAndEntityId(LikeType entityType, String entityId);

    // 특정 사용자의 좋아요 삭제
    void deleteByEntityTypeAndEntityIdAndUserId(LikeType entityType, String entityId, String userId);

    boolean existsByEntityTypeAndEntityIdAndUserId(LikeType entityType, String entityId, String userId);
}
