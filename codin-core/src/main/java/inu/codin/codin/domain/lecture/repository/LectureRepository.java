package inu.codin.codin.domain.lecture.repository;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.lecture.entity.LectureEntity;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LectureRepository extends MongoRepository<LectureEntity, ObjectId> {
    Page<LectureEntity> findAllByDepartment(Pageable pageable, Department department);

    @Query("{ '$or': [ { 'lectureNm': { $regex: ?0, $options: 'i' } }, " +
            "{ 'professor': { $regex: ?0, $options: 'i' } }, " +
            "{ 'lectureCode': { $regex: ?0, $options: 'i' } } ] }")
    Page<LectureEntity> findAllByKeyword(String keyword, Pageable pageable);

}
