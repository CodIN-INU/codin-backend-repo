package inu.codin.codin.domain.post.domain.comment.repository;

import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends MongoRepository<CommentEntity, ObjectId> {


    @Query("{ '_id': ?0, 'deletedAt': null }")
    Optional<CommentEntity> findByIdAndNotDeleted(ObjectId Id);

    @Query("{ 'postId': ?0 }")
    List<CommentEntity> findByPostId(ObjectId postId);
}
