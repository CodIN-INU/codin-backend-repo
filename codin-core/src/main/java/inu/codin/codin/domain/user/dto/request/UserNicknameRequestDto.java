package inu.codin.codin.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.beans.ConstructorProperties;

@Getter
public class UserNicknameRequestDto {

    @Schema(description = "닉네임", example = "코딩")
    @NotBlank
    private String nickname;

    @ConstructorProperties({"nickname"})
    public UserNicknameRequestDto(String nickname) {
        this.nickname = nickname;
    }
}