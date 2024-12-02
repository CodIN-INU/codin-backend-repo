package inu.codin.codin.domain.post.like.entity;

import lombok.Getter;

@Getter
public enum LikeType {
    post("게시물"),
    comment("댓글"),
    reply("대댓글");

    private final String description;

    LikeType(String description) {
        this.description = description;
    }
}
