package inu.codin.codin.domain.board.notice.repository;

import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Repository
public interface NoticeRepository extends MongoRepository<PostEntity, ObjectId> {
    @Query("{'deletedAt': null, " +
            "'postStatus': { $in: ['ACTIVE'] }, " +
            "'title': ?0," +
            "'postCategory': { $in: ?1 }}")
    Page<PostEntity> getNoticesByCategory(Pattern prefixPattern, List<PostCategory> postCategories, Pageable pageable);

    @Query("{'_id':  ?0, 'deletedAt': null, 'postStatus':  { $in:  ['ACTIVE'] }}")
    Optional<PostEntity> findByIdAndNotDeleted(ObjectId Id);
}
