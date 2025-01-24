package inu.codin.codin.domain.user.security;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.entity.UserRole;
import inu.codin.codin.domain.user.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {

    private final ObjectId id;
    private final String email;
    private final String password;
    private final String studentId;
    private final String name;
    private final String nickname;
    private final String profileImageUrl;
    private final Department department;
    private final UserRole role;
    private final UserStatus status;

    private final Collection<? extends GrantedAuthority> authorities;

    @Builder
    public CustomUserDetails(ObjectId id, String email, String password, String studentId, String name, String nickname, String profileImageUrl, Department department, UserRole role, UserStatus status, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.studentId = studentId;
        this.name = name;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.department = department;
        this.role = role;
        this.status = status;
        this.authorities = authorities;
    }

    public static CustomUserDetails from(UserEntity userEntity) {
        return CustomUserDetails.builder()
                .id(userEntity.get_id())
                .email(userEntity.getEmail())
                .password(userEntity.getPassword())
                .studentId(userEntity.getStudentId())
                .name(userEntity.getName())
                .nickname(userEntity.getNickname())
                .profileImageUrl(userEntity.getProfileImageUrl())
                .department(userEntity.getDepartment())
                .role(userEntity.getRole())
                .status(userEntity.getStatus())
                .authorities(Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name())))
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Spring Security에서 사용하는 유저네임은 studentId로 사용
     */
    @Override
    public String getUsername() {
        return studentId;
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정 만료 여부
        return !status.equals(UserStatus.DISABLED);
    }

    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠금 여부
        return !status.equals(UserStatus.SUSPENDED);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 계정 활성화 여부
        return status.equals(UserStatus.ACTIVE);
    }
}
