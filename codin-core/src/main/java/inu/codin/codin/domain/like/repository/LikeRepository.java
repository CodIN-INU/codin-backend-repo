package inu.codin.codin.domain.like.repository;

import inu.codin.codin.domain.like.dto.LikedResponseDto;
import inu.codin.codin.domain.like.entity.LikeEntity;
import inu.codin.codin.domain.like.entity.LikeType;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface LikeRepository extends MongoRepository<LikeEntity, ObjectId> {
    // 특정 엔티티(게시글/댓글/대댓글)의 좋아요 개수 조회
    int countByLikeTypeAndLikeTypeIdAndDeletedAtIsNull(LikeType likeType, String likeTypeId);
    boolean existsByLikeTypeAndLikeTypeIdAndUserIdAndDeletedAtIsNull(LikeType likeType, String id, ObjectId userId);
    Optional<LikeEntity> findByLikeTypeAndLikeTypeIdAndUserId(LikeType likeType, String likeTypeId, ObjectId userId);
    Page<LikeEntity> findAllByUserIdAndLikeTypeAndDeletedAtIsNullOrderByCreatedAt(ObjectId userId, LikeType likeType, Pageable pageable);

    @Query(value = "{ 'likeType': ?0, 'userId': ?1 }", fields = "{ 'likeTypeId': 1, '_id': 0 }")
    List<LikedResponseDto> findLikeTypeIdByLikeTypeAndUserId(LikeType likeType, ObjectId userId);
}
