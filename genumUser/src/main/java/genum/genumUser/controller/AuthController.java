package genum.genumUser.controller;

import genum.genumUser.service.AuthService;
import genum.genumUser.service.GenumUserService;
import genum.shared.DTO.request.LoginRequest;
import genum.shared.DTO.response.ResponseDetails;
import genum.shared.security.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDetails<LoginResponse>> login(@RequestBody LoginRequest loginRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(),loginRequest.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        authService.addJWTtoHeader(httpServletRequest,httpServletResponse,authentication);
        var loginResponse = new LoginResponse("Welcome", httpServletRequest.getHeader("Authorization").substring(7));
        return ResponseEntity
                .created(URI.create(httpServletRequest.getRequestURI()))
                .body(new ResponseDetails<>(LocalDateTime.now() ,
                        "Login Successful",
                        HttpStatus.CREATED.toString(), loginResponse)
                        );
    }
}
