package inu.codin.codin.domain.chat.chatroom.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ParticipantInfo extends BaseTimeEntity {

    private ObjectId userId;
    private boolean isConnected = false;
    private int unreadMessage = 0;

    private boolean isLeaved = false;
    private LocalDateTime whenLeaved;
    private boolean notificationsEnabled = true;

    @Builder
    public ParticipantInfo(ObjectId userId, boolean isConnected, int unreadMessage, boolean notificationsEnabled, boolean isLeaved, LocalDateTime whenLeaved) {
        this.userId = userId;
        this.isConnected = isConnected;
        this.unreadMessage = unreadMessage;
        this.notificationsEnabled = notificationsEnabled;
        this.isLeaved = isLeaved;
        this.whenLeaved = whenLeaved;
    }

    public void updateNotification() {
        this.notificationsEnabled = !notificationsEnabled;
    }

    public static ParticipantInfo enter(ObjectId userId){
        return ParticipantInfo.builder()
                .userId(userId)
                .isConnected(false)
                .unreadMessage(0)
                .isLeaved(false)
                .whenLeaved(null)
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

    public void leave(){
        this.isLeaved = true;
        this.whenLeaved = LocalDateTime.now();
    }

    public void remain(){
        this.isLeaved = false;
    }

}
