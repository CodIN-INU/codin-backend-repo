package inu.codin.codin.domain.chat.chatroom.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@Getter
@Setter
public class Participants {

    private final ObjectId userId;
    private boolean notificationsEnabled;

    @Builder
    public Participants(ObjectId userId, boolean notificationsEnabled) {
        this.userId = userId;
        this.notificationsEnabled = notificationsEnabled;
    }

    public void updateNotification() {
        this.notificationsEnabled = !notificationsEnabled;
    }
}
