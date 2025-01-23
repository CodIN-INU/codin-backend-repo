package inu.codin.codin.domain.lecture.repository;

import inu.codin.codin.domain.lecture.entity.LectureEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LectureRepository extends MongoRepository<LectureEntity, ObjectId> {

}
