package inu.codin.codin.domain.post.repository;

import inu.codin.codin.domain.post.entity.PostEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<PostEntity, String> {

    @Query("{'_id':  ?0, 'deletedAt': null}")
    Optional<PostEntity> findByIdAndNotDeleted(String Id);

    @Query("{'userId': ?0, 'deletedAt': null}")
    List<PostEntity> findByUserIdAndNotDeleted(String userId);

    @Query("{'deletedAt': null}")
    List<PostEntity> findAllAndNotDeleted();
}
