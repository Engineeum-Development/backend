package genum.genumUser.controller;

import genum.genumUser.service.AuthService;
import genum.shared.DTO.request.LoginRequest;
import genum.shared.DTO.response.ResponseDetails;
import genum.shared.security.LoginResponse;
import genum.shared.security.exception.LoginFailedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> auth2AuthorizationRequestRepository;


    @PostMapping("/login")
    public ResponseEntity<ResponseDetails<LoginResponse>> login(@RequestBody LoginRequest loginRequest, HttpServletResponse httpServletResponse) {
        try {
            var token = authService.handleUserLogin(loginRequest, httpServletResponse);
            var loginResponse = new LoginResponse("Welcome", token);
            return ResponseEntity
                    .ok()
                    .body(new ResponseDetails<>(LocalDateTime.now() ,
                            "Login Successful",
                            HttpStatus.OK.toString(), loginResponse)
                            );
        } catch (LoginFailedException e) {
            var loginResponse = new LoginResponse(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ResponseDetails<>("Login Failed", HttpStatus.UNAUTHORIZED.toString(), loginResponse)
            );
        }
    }

    //The endpoint for Google Oauth login is {BaseUrl}/oauth2/authorization/google
}
