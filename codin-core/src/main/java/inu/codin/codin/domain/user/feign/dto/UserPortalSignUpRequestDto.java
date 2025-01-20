package inu.codin.codin.domain.user.feign.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserPortalSignUpRequestDto {

    private String _enpass_login_;

    private String username;

    private String password;

    @Builder
    public UserPortalSignUpRequestDto(String _enpass_login_, String username, String password) {
        this._enpass_login_ = _enpass_login_;
        this.username = username;
        this.password = password;
    }
}
