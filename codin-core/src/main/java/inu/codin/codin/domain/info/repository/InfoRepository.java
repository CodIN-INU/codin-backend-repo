package inu.codin.codin.domain.info.repository;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.domain.lab.entity.Lab;
import inu.codin.codin.domain.info.domain.office.entity.Office;
import inu.codin.codin.domain.info.domain.professor.entity.Professor;
import inu.codin.codin.domain.info.entity.Info;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InfoRepository extends MongoRepository<Info, String> {

    @Query("{ 'info_type': 'LAB' }")
    List<Lab> findAllLabs();

    @Query("{ 'info_type': 'LAB', 'id':  ?0}")
    Optional<Lab> findLabById(String id);

    @Query("{ 'info_type': 'OFFICE' }")
    List<Office> findAllOffices();

    @Query("{ 'info_type':  'OFFICE', 'department':  ?0}")
    List<Office> findOfficeByDepartment(Department department);

    @Query("{ 'info_type': 'PROFESSOR' , 'department':  ?0}")
    List<Professor> findAllProfessorsByDepartment(Department department);

    @Query("{ 'info_type': 'PROFESSOR' , 'id':  ?0}")
    Optional<Professor> findProfessorById(String id);

    @Query("{ 'info_type': 'PROFESSOR'}")
    List<Professor> findAllProfessor();


}



