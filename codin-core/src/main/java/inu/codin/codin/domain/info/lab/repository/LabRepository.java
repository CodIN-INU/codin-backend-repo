package inu.codin.codin.domain.info.lab.repository;

import inu.codin.codin.domain.info.lab.entity.Lab;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LabRepository extends MongoRepository<Lab, String> {
}
