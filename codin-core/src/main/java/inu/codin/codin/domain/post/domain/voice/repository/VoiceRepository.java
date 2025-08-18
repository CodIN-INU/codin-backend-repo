package inu.codin.codin.domain.post.domain.voice.repository;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.domain.post.domain.voice.entity.VoiceEntity;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoiceRepository extends MongoRepository<VoiceEntity, ObjectId> {

    // 특정 학과의 보이스박스 페이징 조회 (삭제되지 않은 것만)
    @Query("{'department': ?0, 'answer': {$ne: null}, 'deletedAt': null}")
    Page<VoiceEntity> findAnsweredByDepartmentAndNotDeleted(Department department, Pageable pageable);

    // 답변이 없는 보이스박스 페이징 조회 (삭제되지 않은 것만)
    @Query("{'department': ?0, 'answer': null, 'deletedAt': null}")
    Page<VoiceEntity> findByDepartmentAndAnswerIsNullAndNotDeleted(Department department, Pageable pageable);

    // ID로 삭제되지 않은 보이스박스 조회
    @Query("{'_id': ?0, 'deletedAt': null}")
    Optional<VoiceEntity> findByIdAndNotDeleted(ObjectId id);

}
