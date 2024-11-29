package inu.codin.codin.domain.post.comment.repository;


import inu.codin.codin.domain.post.comment.entity.ReplyEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReplyRepository extends MongoRepository<ReplyEntity, String> {
    List<ReplyEntity> findByCommentId(String commentId);
}
