package inu.codin.codin.domain.chat.chatroom.entity;

import inu.codin.codin.common.BaseTimeEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class Participants {

    private Map<ObjectId, ParticipantInfo> info = new ConcurrentHashMap<>();

    public void create(ObjectId memberId){
        info.put(memberId, ParticipantInfo.enter(memberId));
    }

    public boolean getMessage(ObjectId receiver) {
        ParticipantInfo member;
        if((member=info.get(receiver))==null) {
            return false;
        }
        member.plusUnread();
        return true;
    }

    public boolean enter(ObjectId memberId){
        ParticipantInfo participantInfo;
        if ((participantInfo = info.get(memberId))==null) {
            return false;
        }

        participantInfo.connect();
        info.put(memberId, participantInfo);
        return true;
    }

    public boolean exit(ObjectId memberId) {
        ParticipantInfo participantInfo;
        if ((participantInfo = info.get(memberId))==null) {
            return false;
        }

        participantInfo.disconnect();
        info.put(memberId, participantInfo);
        return true;
    }
}
