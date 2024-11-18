package inu.codin.codin.common.security.jwt;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {

    public JwtAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public JwtAuthenticationToken(UserDetails userDetails, Collection<? extends GrantedAuthority> authorities) {
        super(userDetails, null, authorities);
    }

    @Override
    public Object getPrincipal() {
        return (UserDetails) super.getPrincipal();
    }

    @Override
    public Object getCredentials() {
        return null;
    }
}
