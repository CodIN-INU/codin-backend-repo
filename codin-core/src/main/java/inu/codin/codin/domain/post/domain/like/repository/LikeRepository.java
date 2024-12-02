package inu.codin.codin.domain.post.domain.like.repository;

import inu.codin.codin.domain.post.domain.like.entity.LikeEntity;
import inu.codin.codin.domain.post.domain.like.entity.LikeType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface LikeRepository extends MongoRepository<LikeEntity, String> {
    // 특정 엔티티(게시글/댓글/대댓글)의 좋아요 개수 조회
    long countByLikeTypeAndLikeTypeId(LikeType likeType, String id);

    // 특정 엔티티의 좋아요 데이터 조회
    List<LikeEntity> findByLikeTypeAndLikeTypeId(LikeType likeType, String id);

    // 특정 사용자의 좋아요 삭제
    void deleteByLikeTypeAndLikeTypeIdAndUserId(LikeType likeType, String id, String userId);

    boolean existsByLikeTypeAndLikeTypeIdAndUserId(LikeType likeType, String id, String userId);
}
