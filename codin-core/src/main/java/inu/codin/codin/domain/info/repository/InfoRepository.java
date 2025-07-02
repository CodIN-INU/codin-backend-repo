package inu.codin.codin.domain.info.repository;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.domain.info.entity.Lab;
import inu.codin.codin.domain.info.entity.Office;
import inu.codin.codin.domain.info.entity.Professor;
import inu.codin.codin.domain.info.entity.Info;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InfoRepository extends MongoRepository<Info, ObjectId> {

    @Query("{ 'infoType': 'LAB', 'deletedAt': null }")
    List<Lab> findAllLabs();

    @Query("{ 'infoType': 'LAB', '_id': ?0, 'deletedAt': null }")
    Optional<Lab> findLabById(ObjectId id);

    @Query("{ 'infoType': 'OFFICE', 'department': ?0, 'deletedAt': null }")
    Office findOfficeByDepartment(Department department);

    @Query("{ 'infoType': 'PROFESSOR', 'department': ?0, 'deletedAt': null }")
    List<Professor> findAllProfessorsByDepartment(Department department);

    @Query("{ 'infoType': 'PROFESSOR', '_id': ?0, 'deletedAt': null }")
    Optional<Professor> findProfessorById(ObjectId id);

    @Query("{ 'infoType': 'PROFESSOR', 'email': ?0, 'deletedAt': null }")
    Optional<Professor> findProfessorByEmail(String email);


}



