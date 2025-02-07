package inu.codin.codin.domain.notification.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 알림 설정
@NoArgsConstructor
@Getter
public class NotificationPreference {

    // 푸시 알림 허용 여부
    private boolean allowPush = true;

    // 이메일 알림 허용 여부
    private boolean allowEmail = true;

    // 알림 설정
    private Preference preference = new Preference();

    @Builder
    public NotificationPreference(boolean allowPush, boolean allowEmail, Preference preference) {
        this.allowPush = allowPush;
        this.allowEmail = allowEmail;
        this.preference = preference;
    }

    @Getter
    @Setter
    class Preference {
        // 댓글 알림
        private boolean notifyOnComment = true;
        // 좋아요 알림
        private boolean notifyOnLike = true;
        // 답글 알림
        private boolean notifyOnReply = true;
        // 베스트 게시글 선정 알림
        private boolean notifyOnBestPost = true;
    }
}
