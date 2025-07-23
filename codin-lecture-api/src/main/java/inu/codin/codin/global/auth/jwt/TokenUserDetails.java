package inu.codin.codin.global.auth.jwt;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 토큰 검증 전용 UserDetails - DB 조회 없이 JWT 토큰에서 파싱한 정보만 담음
 */
@Getter
public class TokenUserDetails implements UserDetails {

    private final String email;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    @Builder
    public TokenUserDetails(String email, String role) {
        this.email = email;
        this.role = role;
        // JWT에서 이미 ROLE_ 접두사가 있는 경우 그대로 사용, 없는 경우 추가
        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(authority));
    }

    public static TokenUserDetails fromTokenClaims(String email, String role) {
        return TokenUserDetails.builder()
                .email(email)
                .role(role)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
