package inu.codin.codin.domain.voice.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.common.dto.Department;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "voice_boxes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoiceEntity extends BaseTimeEntity {

    @Id
    private ObjectId id;

    private Department department;

    private List<ObjectId> positiveVoteIds = new ArrayList<>();

    private List<ObjectId> oppositeVoteIds = new ArrayList<>();

    private String question;

    private String answer;

    @Builder
    public VoiceEntity(Department department, List<ObjectId> positiveVoteIds, List<ObjectId> oppositeVoteIds, String question, String answer) {
        this.department = department;
        this.positiveVoteIds = positiveVoteIds != null ? positiveVoteIds : new ArrayList<>();;
        this.oppositeVoteIds = oppositeVoteIds != null ? oppositeVoteIds : new ArrayList<>();;
        this.question = question;
        this.answer = answer;
    }

    /**
     * 긍정에 투표 토글 메서드 (중복 X)
     * @param userId 투표 유저 ObjectId
     */
    public void votePositiveToggle(ObjectId userId) {
        if (positiveVoteIds.remove(userId)) {
            return;
        }
        oppositeVoteIds.remove(userId);
        positiveVoteIds.add(userId);
    }

    /**
     * 부정에 투표 토글 메서드 (중복 X)
     * @param userId 투표 유저 ObjectId
     */
    public void voteOppositeToggle(ObjectId userId) {
        if (oppositeVoteIds.remove(userId)) {
            return;
        }
        positiveVoteIds.remove(userId);
        oppositeVoteIds.add(userId);
    }

    public void updateAnswer(String answer) {
        this.answer = answer;
    }
}
