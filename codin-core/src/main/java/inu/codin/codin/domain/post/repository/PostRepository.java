package inu.codin.codin.domain.post.repository;

import inu.codin.codin.domain.post.entity.PostEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<PostEntity, String> {
    Optional<PostEntity> findByTitle(String title);

    List<PostEntity> findByUserId(String userId);

    @Query("{'userId': ?0, 'isDeleted': false}")
    List<PostEntity> findByUserIdNotDeleted(String userId);

    @Query("{'isDeleted': false}")
    List<PostEntity> findALlNotDeleted();

    @Query("{'postId': ?0, 'isDeleted': false}")
    PostEntity findByPostIdNotDeleted(String postId);


}
