package inu.codin.codin.domain.post.entity;

import lombok.Getter;

@Getter
public enum PostStatus {
    ACTIVE("활성"),
    DISABLED("비활성"),
    SUSPENDED("정지");

    private final String description;

    PostStatus(String description) {
        this.description = description;
    }

}