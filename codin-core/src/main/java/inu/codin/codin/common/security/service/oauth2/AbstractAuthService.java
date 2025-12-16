package inu.codin.codin.common.security.service;

import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.s3.S3Service;
import inu.codin.security.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractAuthService {
    protected final UserRepository userRepository;
    protected final S3Service s3Service;
    protected final JwtService jwtService;
    protected final UserDetailsService userDetailsService;

    protected void issueJwtToken(String identifier, HttpServletResponse response) {
        // TODO: Phase 2에서 codin-auth로 이동 예정
        // 토큰 발급 기능은 Authorization Server에서 담당
        // jwtService.deleteToken(response);
        // UserDetails userDetails = userDetailsService.loadUserByUsername(identifier);
        // UsernamePasswordAuthenticationToken authToken =
        //         new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        // SecurityContextHolder.getContext().setAuthentication(authToken);
        // jwtService.createToken(response);
        throw new UnsupportedOperationException("토큰 발급 기능은 Phase 2에서 codin-auth 모듈로 이동 예정");
    }

}
