package inu.codin.codin.common.security.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private final HttpSession httpSession;

    public void setRedirectHostSession(String redirectHost) {
        if (!Objects.equals(redirectHost, null)) httpSession.setAttribute("redirect_host", redirectHost);
    }

    public void setRedirectPathSession(String redirectPath) {
        if (!Objects.equals(redirectPath, null)) httpSession.setAttribute("redirect_path", redirectPath);
    }
}
