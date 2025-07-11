package inu.codin.codin.domain.post.dto;

import lombok.Getter;

@Getter
public class UserProfile {
    private String nickname;
    private String imageUrl;
    private boolean isDeleted;

    public UserProfile(String nickname, String imageUrl, boolean isDeleted) {
        this.nickname = nickname;
        this.imageUrl = imageUrl;
        this.isDeleted = isDeleted;
    }

    public UserProfile(String nickname, String imageUrl) {
        this.nickname = nickname;
        this.imageUrl = imageUrl;
    }

    public static UserProfile of(String nickname, String imageUrl) {
        return new UserProfile(nickname, imageUrl);
    }
}
