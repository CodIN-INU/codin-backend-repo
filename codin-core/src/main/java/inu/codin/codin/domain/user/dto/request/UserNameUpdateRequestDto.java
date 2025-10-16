package inu.codin.codin.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.beans.ConstructorProperties;

@Getter
public class UserNameUpdateRequestDto {

    @Schema(description = "이름", example = "횃불이")
    @NotBlank(message = "이름은 비어 있을 수 없습니다.")
    @Size(min = 1, max = 10, message = "이름은 1~10자여야 합니다.")
    private String name;

    @ConstructorProperties({"name"})
    public UserNameUpdateRequestDto(String name) {
        this.name = name;
    }
}