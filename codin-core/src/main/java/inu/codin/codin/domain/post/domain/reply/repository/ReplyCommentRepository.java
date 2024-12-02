package inu.codin.codin.domain.post.domain.reply.repository;


import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReplyCommentRepository extends MongoRepository<ReplyCommentEntity, String> {
    List<ReplyCommentEntity> findByCommentId(String commentId);
}
