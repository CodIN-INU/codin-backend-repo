package inu.codin.codin.domain.post.domain.like.entity;

import lombok.Getter;

@Getter
public enum LikeType {
    POST("게시물"),
    COMMENT("댓글"),
    REPLY("대댓글");

    private final String description;

    LikeType(String description) {
        this.description = description;
    }
}
