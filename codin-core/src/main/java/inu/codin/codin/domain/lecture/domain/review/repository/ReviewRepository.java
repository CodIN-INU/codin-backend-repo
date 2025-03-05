package inu.codin.codin.domain.lecture.domain.review.repository;

import inu.codin.codin.domain.lecture.domain.review.entity.ReviewEntity;
import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<ReviewEntity, ObjectId> {
    Page<ReviewEntity> findAllByLectureIdAndDeletedAtIsNull(ObjectId lectureId, PageRequest pageRequest);

    Optional<ReviewEntity> findByLectureIdAndUserIdAndDeletedAtIsNull(ObjectId lectureId, ObjectId userId);

    Optional<ReviewEntity> findByLectureIdAndDeletedAtIsNull(ObjectId Id);
}
