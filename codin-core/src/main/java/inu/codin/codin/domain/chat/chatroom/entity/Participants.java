package inu.codin.codin.domain.chat.chatroom.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Participants {

    private final String userId;
    private boolean notificationsEnabled;

    @Builder
    public Participants(String userId, boolean notificationsEnabled) {
        this.userId = userId;
        this.notificationsEnabled = notificationsEnabled;
    }

    public void updateNotification() {
        this.notificationsEnabled = !notificationsEnabled;
    }
}
