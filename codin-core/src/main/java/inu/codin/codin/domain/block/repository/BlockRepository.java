package inu.codin.codin.domain.block.repository;

import inu.codin.codin.domain.block.entity.BlockEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface BlockRepository extends MongoRepository<BlockEntity, ObjectId> {
    @Query("{'_id':  ?0, 'deletedAt': null}")
    Optional<BlockEntity> findByUserId(ObjectId userId);
}