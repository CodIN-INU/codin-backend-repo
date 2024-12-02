package inu.codin.codin.domain.post.repository;

import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<PostEntity, String> {

    @Query("{'_id':  ?0, 'deletedAt': null, 'postStatus':  { $in:  ['ACTIVE'] }}")
    Optional<PostEntity> findByIdAndNotDeleted(String Id);

    @Query("{'userId': ?0, 'deletedAt': null, 'postStatus':  { $in:  ['ACTIVE'] }}")
    List<PostEntity> findByUserIdAndNotDeleted(String userId);

    @Query("{'deletedAt': null, 'postStatus':  { $in:  ['ACTIVE'] }, 'postCategory': ?0 }")
    List<PostEntity> findAllAndNotDeletedAndActive(PostCategory postCategory);
}
