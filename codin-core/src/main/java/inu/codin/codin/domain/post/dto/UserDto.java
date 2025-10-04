package inu.codin.codin.domain.post.dto;

import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.user.entity.UserEntity;
import lombok.Getter;

@Getter
public class UserDto {

    private final String nickname;
    private final String imageUrl;
    private final boolean deleted;

    private UserDto(String nickname, String imageUrl, boolean deleted) {
        this.nickname = nickname;
        this.imageUrl = imageUrl;
        this.deleted = deleted;
    }

    /** 게시물용 사용자 DTO */
    public static UserDto forPost(PostEntity post, UserEntity user, String defaultProfileImageUrl) {
        if (isDeleted(user)) return deletedUser(user, defaultProfileImageUrl);
        if (post.isAnonymous()) {
            return of("익명", defaultProfileImageUrl, false);
        }
        return normalUser(user, defaultProfileImageUrl);
    }

    /** 댓글용 사용자 DTO */
    public static UserDto forComment(CommentEntity comment, UserEntity user, int anonNum, String defaultProfileImageUrl) {
        if (isDeleted(user)) return deletedUser(user, defaultProfileImageUrl);
        if (comment.isAnonymous()) {
            String nick = anonNickname(anonNum);
            return of(nick, defaultProfileImageUrl, false);
        }
        return normalUser(user, defaultProfileImageUrl);
    }

    /** 대댓글용 사용자 DTO */
    public static UserDto forReply(ReplyCommentEntity reply, UserEntity user, int anonNum, String defaultProfileImageUrl) {
        if (isDeleted(user)) return deletedUser(user, defaultProfileImageUrl);
        if (reply.isAnonymous()) {
            String nick = anonNickname(anonNum);
            return of(nick, defaultProfileImageUrl, false);
        }
        return normalUser(user, defaultProfileImageUrl);
    }

    // ---------- Private helpers ----------
    private static boolean isDeleted(UserEntity user) {
        return user.getDeletedAt() != null;
    }

    private static UserDto deletedUser(UserEntity user, String defaultProfileImageUrl) {
        String image = withDefault(user.getProfileImageUrl(), defaultProfileImageUrl);
        return of(user.getNickname(), image, true);
    }

    private static UserDto normalUser(UserEntity user, String defaultProfileImageUrl) {
        String image = withDefault(user.getProfileImageUrl(), defaultProfileImageUrl);
        return of(user.getNickname(), image, false);
    }

    private static String anonNickname(int anonNum) {
        return anonNum == 0 ? "글쓴이" : "익명" + anonNum;
    }

    private static String withDefault(String value, String defaultValue) {
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private static UserDto of(String nickname, String imageUrl, boolean deleted) {
        return new UserDto(nickname, imageUrl, deleted);
    }
}