package inu.codin.codin.domain.info.professor.repository;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.professor.entity.Professor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProfessorRepository extends MongoRepository<Professor, String> {

    List<Professor> findAllByDepartment(Department department);
}
