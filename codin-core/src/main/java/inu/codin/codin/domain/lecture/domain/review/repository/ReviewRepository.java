package inu.codin.codin.domain.lecture.domain.review.repository;

import inu.codin.codin.domain.lecture.domain.review.entity.ReviewEntity;
import inu.codin.codin.domain.lecture.dto.Emotion;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<ReviewEntity, ObjectId> {

    @Aggregation(pipeline = {
            "{ '$match': { 'lectureId': ?0 } }",
            "{ '$group': { '_id': null, 'avgRating': { '$avg': '$starRating' } } }"
    })
    Double getAvgRatingByLectureId(ObjectId lectureId);

    @Aggregation(pipeline = {
            "{ '$match': { 'lectureId': ?0 } }",
            "{ '$group': { " +
                    "  '_id': null, " +
                    "  'hard': { '$sum': { '$cond': [ { '$and': [ { '$gte': [ '$starRating', 0.25 ] }, { '$lte': [ '$starRating', 1.5 ] } ] }, 1, 0 ] } }, " +
                    "  'ok': { '$sum': { '$cond': [ { '$and': [ { '$gt': [ '$starRating', 1.75 ] }, { '$lte': [ '$starRating', 3.5 ] } ] }, 1, 0 ] } }, " +
                    "  'best': { '$sum': { '$cond': [ { '$and': [ { '$gt': [ '$starRating', 3.75 ] }, { '$lte': [ '$starRating', 5.0 ] } ] }, 1, 0 ] } } " +
                    "} }",
            "{ '$project': { '_id': 0, 'hard': 1, 'ok': 1, 'best': 1 } }"
    })
    Emotion getEmotionsCountByRanges(ObjectId lectureId);

    int countByLectureId(ObjectId lectureId);

    Page<ReviewEntity> getAvgRatingByLectureId(ObjectId lectureId, PageRequest pageRequest);

    Optional<ReviewEntity> findByLectureIdAndUserIdAndDeletedAtIsNull(ObjectId lectureId, ObjectId userId);

    Optional<ReviewEntity> findBy_idAndDeletedAtIsNull(ObjectId Id);
}
