package inu.codin.codin.domain.info.repository;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.domain.lab.entity.Lab;
import inu.codin.codin.domain.info.domain.office.entity.Office;
import inu.codin.codin.domain.info.domain.professor.entity.Professor;
import inu.codin.codin.domain.info.entity.Info;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InfoRepository extends MongoRepository<Info, String> {

    @Query("{ 'infoType': 'LAB' }")
    List<Lab> findAllLabs();

    @Query("{ 'infoType': 'LAB', '_id':  ?0}")
    Optional<Lab> findLabById(String id);

    @Query("{ 'infoType': 'OFFICE' }")
    List<Office> findAllOffices();

    @Query("{ 'infoType':  'OFFICE', 'department':  ?0}")
    Office findOfficeByDepartment(Department department);

    @Query("{ 'infoType': 'PROFESSOR' , 'department':  ?0}")
    List<Professor> findAllProfessorsByDepartment(Department department);

    @Query("{ 'infoType': 'PROFESSOR' , '_id':  ?0}")
    Optional<Professor> findProfessorById(String id);

    @Query("{ 'infoType': 'PROFESSOR' , 'email': ?0}")
    Optional<Professor> findProfessorByEmail(String email);


}



