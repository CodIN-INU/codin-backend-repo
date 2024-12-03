package inu.codin.codin.domain.post.domain.reply.repository;


import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReplyCommentRepository extends MongoRepository<ReplyCommentEntity, String> {

    @Query("{'_id': ?0, 'deletedAt': null}")
    Optional<ReplyCommentEntity> findByIdAndNotDeleted(String id);

    @Query("{'commentId':  ?0, 'deletedAt':  null}")
    List<ReplyCommentEntity> findByCommentIdAndNotDeleted(String commentId);
}
