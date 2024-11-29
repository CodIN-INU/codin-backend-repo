package inu.codin.codin.domain.post.comment.repository;

import inu.codin.codin.domain.post.comment.entity.CommentEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<CommentEntity, String> {
    List<CommentEntity> findByPostId(String postId);

    int countByPostId(@NotBlank String postId);
}
