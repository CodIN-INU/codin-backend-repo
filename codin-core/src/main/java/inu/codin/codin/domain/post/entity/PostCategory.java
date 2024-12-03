package inu.codin.codin.domain.post.entity;

import lombok.Getter;

@Getter
public enum PostCategory {
    REQUEST_STUDY("구해요_스터디"),
    REQUEST_PROJECT("구해요_프로젝트"),
    REQUEST_COMPETITION("구해요_공모전_대회"),
    REQUEST_GROUP("구해요_소모임"),

    COMMUNICATION_QUESTION("소통해요_질문"),
    COMMUNICATION_JOB("소통해요_취업수기"),
    COMMUNICATION_TIP("소통해요_꿀팁공유"),

    EXTRACURRICULAR_STARINU("비교과_STARINU"),
    EXTRACURRICULAR_OUTER("비교과_교외"),
    EXTRACURRICULAR_INNER("비교과_교내");

    private final String description;

    PostCategory(String description) {
        this.description = description;
    }
}