package inu.codin.codin.domain.user.dto.response;

import inu.codin.codin.domain.user.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoResponseDto {
    private String _id;
    private String email;
    private String name;
    private String profileImageUrl;

    @Builder
    public UserInfoResponseDto(String _id, String email, String name, String profileImageUrl) {
        this._id = _id;
        this.email = email;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }

    public static UserInfoResponseDto of(UserEntity user) {
        return UserInfoResponseDto.builder()
                ._id(user.get_id().toString())
                .email(user.getEmail())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}
