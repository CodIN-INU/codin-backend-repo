package inu.codin.codin.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserInfo {

    @Schema(description = "좋아요 여부", example = "true")
    private final Boolean like;

    //해당 값이 null일 경우 직렬화(JSON 변환) 시 필드 제외
    @Schema(description = "스크랩 여부", example = "false", nullable = true)
    @JsonInclude(Include.NON_NULL)
    private final Boolean scrap;

    //해당 값이 null일 경우 직렬화(JSON 변환) 시 필드 제외
    @Schema(description = "내가 쓴 글여부", example = "false", nullable = true)
    @JsonInclude(Include.NON_NULL)
    private final Boolean mine;

    @Builder
    private UserInfo(Boolean like, Boolean scrap, Boolean mine) {
        this.like = like;
        this.scrap = scrap;
        this.mine = mine;
    }

    public static UserInfo ofPost(boolean like, boolean scrap, boolean mine) {
        return UserInfo.builder()
                .like(like)
                .scrap(scrap)
                .mine(mine)
                .build();
    }

    public static UserInfo ofComment(boolean like) {
        return UserInfo.builder()
                .like(like)
                .scrap(null)
                .mine(null)
                .build();
    }
} 