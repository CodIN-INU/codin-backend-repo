package inu.codin.codin.domain.lecture.domain.review.repository;

import inu.codin.codin.domain.lecture.domain.review.entity.ReviewEntity;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<ReviewEntity, ObjectId> {
    Page<ReviewEntity> findAllByLectureId(ObjectId lectureId, PageRequest pageRequest);

    Optional<ReviewEntity> findByLectureIdAndUserId(ObjectId lectureId, ObjectId userId);
}
