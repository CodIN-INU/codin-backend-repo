package inu.codin.codin.domain.board.question.repository;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.domain.board.question.entity.QuestionEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<QuestionEntity, ObjectId> {
    List<QuestionEntity> findAllByDepartment(Department department);
}
