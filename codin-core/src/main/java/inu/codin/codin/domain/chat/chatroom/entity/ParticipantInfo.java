package inu.codin.codin.domain.chat.chatroom.entity;

import inu.codin.codin.common.BaseTimeEntity;
import lombok.*;
import org.bson.types.ObjectId;

@Getter
@NoArgsConstructor
public class ParticipantInfo extends BaseTimeEntity {

    private ObjectId userId;
    private boolean isConnected = false;
    private int unreadMessage = 0;
    private boolean notificationsEnabled = true;

    @Builder
    public ParticipantInfo(ObjectId userId, boolean isConnected, int unreadMessage, boolean notificationsEnabled) {
        this.userId = userId;
        this.isConnected = isConnected;
        this.unreadMessage = unreadMessage;
        this.notificationsEnabled = notificationsEnabled;
    }

    public void updateNotification() {
        this.notificationsEnabled = !notificationsEnabled;
    }

    public static ParticipantInfo enter(ObjectId userId){
        return ParticipantInfo.builder()
                .userId(userId)
                .isConnected(false)
                .unreadMessage(0)
                .notificationsEnabled(true)
                .build();
    }

    public void plusUnread(){
        this.unreadMessage++;
    }

    public void connect(){
        this.isConnected = true;
        this.unreadMessage = 0;
    }

    public void disconnect(){
        this.isConnected = false;
        this.unreadMessage = 0;
        setUpdatedAt();
    }

}
