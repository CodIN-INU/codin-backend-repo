package inu.codin.codin.domain.post.domain.comment.repository;

import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends MongoRepository<CommentEntity, String> {


    @Query("{ '_id': ?0, 'deletedAt': null }")
    Optional<CommentEntity> findByIdAndNotDeleted(String Id);

    @Query("{ 'postId': ?0, 'deletedAt':  null }")
    List<CommentEntity> findByPostIdAndNotDeleted(String postId);
}
