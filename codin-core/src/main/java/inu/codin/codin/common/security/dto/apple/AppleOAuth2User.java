package inu.codin.codin.common.security.dto.apple;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

//Apple OAuth2의 경우 sub을 고유한 식별자로 사용하고, email을 별도로 가져와서 사용하도록 처리

public class AppleOAuth2User implements OAuth2User {
    private final Map<String, Object> attributes;

    public AppleOAuth2User(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public <A> A getAttribute(String name) {
        return OAuth2User.super.getAttribute(name);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getName() {
        return (String) attributes.get("sub"); // Apple의 고유 식별자
    }

    public String getEmail() {
        return (String) attributes.get("email");
    }
}