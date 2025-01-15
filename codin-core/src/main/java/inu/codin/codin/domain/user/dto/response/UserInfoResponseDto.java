package inu.codin.codin.domain.user.dto.response;

import inu.codin.codin.common.Department;
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
    private String nickname;
    private Department department;

    @Builder
    public UserInfoResponseDto(String _id, String email, String name, String profileImageUrl, String nickname, Department department) {
        this._id = _id;
        this.email = email;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.nickname = nickname;
        this.department = department;
    }

    public static UserInfoResponseDto of(UserEntity user) {
        return UserInfoResponseDto.builder()
                ._id(user.get_id().toString())
                .email(user.getEmail())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .nickname(user.getNickname())
                .department(user.getDepartment())
                .build();
    }
}
