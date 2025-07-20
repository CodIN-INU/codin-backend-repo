package inu.codin.codinlectureapi.lecture.repository;

import inu.codin.codin.common.dto.Department;
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

    @Query("{ 'department': ?0, '$or': [ { 'lectureNm': { $regex: ?1, $options: 'i' } }, " +
            "{ 'professor': { $regex: ?1, $options: 'i' } } ] }")
    Page<LectureEntity> findAllByKeywordAndDepartment(Department department, String keyword, Pageable pageable);

}
