package inu.codin.codin.domain.info.office.repository;

import inu.codin.codin.domain.info.office.entity.Office;
import inu.codin.codin.domain.user.entity.Department;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OfficeRepository extends MongoRepository<Office, String> {

    List<Office> findAllByDepartment(@NotBlank Department department);
}
