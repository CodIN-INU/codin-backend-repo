package inu.codin.codin.domain.info.repository;

import inu.codin.codin.domain.info.entity.Partner;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PartnerRepository extends MongoRepository<Partner, ObjectId> {

}
