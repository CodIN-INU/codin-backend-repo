package inu.codin.codin.common.security.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private final HttpSession httpSession;

    public void setSession(String redirectUrl) {
        if (!Objects.equals(redirectUrl, null)) httpSession.setAttribute("redirect_url", redirectUrl);
    }
}
