package inu.codin.codin.domain.post.domain.scrap.repository;

import inu.codin.codin.domain.post.domain.scrap.entity.ScrapEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapRepository extends MongoRepository<ScrapEntity, String> {

    List<ScrapEntity> findByPostId(String postId);

    long countByPostId(String postId);

    void deleteByPostIdAndUserId(String postId, String userId);

    boolean existsByPostIdAndUserId(String postId, String userId);
}
