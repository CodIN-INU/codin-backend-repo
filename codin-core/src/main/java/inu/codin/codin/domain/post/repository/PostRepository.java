package inu.codin.codin.domain.post.repository;

import inu.codin.codin.domain.post.entity.PostEntity;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<PostEntity, ObjectId> {

    @Query("{'_id':  ?0, 'deletedAt': null, 'postStatus':  { $in:  ['ACTIVE'] }}")
    Optional<PostEntity> findByIdAndNotDeleted(ObjectId Id);


    @Query("{'deletedAt': null, 'postStatus':  { $in:  ['ACTIVE'] }, 'userId': ?0 }")
    Page<PostEntity> findAllByUserIdOrderByCreatedAt(ObjectId userId, PageRequest pageRequest);

    @Query("{'deletedAt': null, 'postStatus': { $in: ['ACTIVE'] }, 'postCategory': { $regex: '^?0' } , 'userId': { $nin: ?1 }}")
    Page<PostEntity> getPostsByCategoryWithBlockedUsers(String postCategory, List<ObjectId> blockedUsersId, PageRequest pageRequest);

    @Query("{'deletedAt': null, " +
            "'postStatus': { $in: ['ACTIVE'] }, " +
            "'userId': { $nin: ?0 }, " +
            "'reportCount': { $gte: 1 } }") // 신고 카운트가 1 이상인 경우만 반환
    Page<PostEntity> getPostsWithReported(PageRequest pageRequest);



    @Query("{ '$or': [ "
            +
            "{ 'content': { $regex: ?0, $options: 'i' }, 'userId': { $nin: ?1 }  }, "
            +
            "{ 'title': { $regex: ?0, $options: 'i' }, 'userId': { $nin: ?1 }  } ] }")
    Page<PostEntity> findAllByKeywordAndDeletedAtIsNull(String keyword, List<ObjectId> blockedUsersId, PageRequest pageRequest);

    boolean existsBy_idAndDeletedAtIsNull(ObjectId id);
}
