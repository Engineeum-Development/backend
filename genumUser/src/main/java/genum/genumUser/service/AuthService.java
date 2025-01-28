package genum.genumUser.service;

import genum.genumUser.security.jwt.JwtUtils;
import genum.shared.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtils jwtUtils;

    public void addJWTtoHeader(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        jwtUtils.addHeader(response, (CustomUserDetails) authentication.getPrincipal());
    }


}
