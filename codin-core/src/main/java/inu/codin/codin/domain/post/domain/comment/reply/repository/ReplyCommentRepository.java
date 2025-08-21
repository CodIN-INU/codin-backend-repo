package inu.codin.codin.domain.post.domain.comment.reply.repository;


import inu.codin.codin.domain.post.domain.comment.reply.entity.ReplyCommentEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReplyCommentRepository extends MongoRepository<ReplyCommentEntity, ObjectId> {

    @Query("{'_id': ?0, 'deletedAt': null}")
    Optional<ReplyCommentEntity> findByIdAndNotDeleted(ObjectId id);

    List<ReplyCommentEntity> findByCommentId(ObjectId commentId);
}
