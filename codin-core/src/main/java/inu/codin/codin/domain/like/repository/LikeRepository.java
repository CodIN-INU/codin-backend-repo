package inu.codin.codin.domain.like.repository;

import inu.codin.codin.domain.like.entity.LikeEntity;
import inu.codin.codin.domain.like.entity.LikeType;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface LikeRepository extends MongoRepository<LikeEntity, ObjectId> {
    // 특정 엔티티(게시글/댓글/대댓글)의 좋아요 개수 조회
    long countByLikeTypeAndLikeTypeIdAndDeletedAtIsNull(LikeType likeType, ObjectId id);

    // 특정 엔티티의 좋아요 데이터 조회
    List<LikeEntity> findByLikeTypeAndLikeTypeIdAndDeletedAtIsNull(LikeType likeType, ObjectId id);

    boolean existsByLikeTypeAndLikeTypeIdAndUserIdAndDeletedAtIsNull(LikeType likeType, ObjectId id, ObjectId userId);

    LikeEntity findByLikeTypeAndLikeTypeIdAndUserId(LikeType likeType, ObjectId id, ObjectId userId);

    List<LikeEntity> findByLikeTypeAndDeletedAtIsNull(LikeType likeType);

    Page<LikeEntity> findAllByUserIdAndLikeTypeAndDeletedAtIsNullOrderByCreatedAt(ObjectId userId, LikeType likeType, Pageable pageable);
}
