package genum.genumUser.service;

import genum.genumUser.security.jwt.JwtUtils;
import genum.shared.DTO.request.LoginRequest;
import genum.shared.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final GenumUserService genumUserService;
    private String addJWTtoHeader(HttpServletResponse response, Authentication authentication) {
        return jwtUtils.addHeader(response, (CustomUserDetails) authentication.getPrincipal());
    }
    private void updateUserLastLogin(String userEmail) {
        genumUserService.incrementUserLastLogin(userEmail);
    }

    public String handleUserLogin(LoginRequest loginRequest, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(),loginRequest.password())
        );
        if (!authentication.isAuthenticated()) {
            if (!((CustomUserDetails)authentication.getPrincipal()).isEnabled()){
                throw new LockedException("Account is disabled");
            } else if (((CustomUserDetails)authentication.getPrincipal()).isAccountLocked()) {
                throw new LockedException("Account is locked");
            }
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        updateUserLastLogin(((CustomUserDetails)authentication.getPrincipal()).getEmail());
        return addJWTtoHeader(servletResponse,authentication);
    }



}
