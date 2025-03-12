package genum.genumUser.controller;

import genum.genumUser.service.AuthService;
import genum.shared.DTO.request.LoginRequest;
import genum.shared.DTO.response.ResponseDetails;
import genum.shared.security.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
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


    private final AuthService authService;
    private final ClientRegistrationRepository clientRegistrationRepository;


    @PostMapping("/login")
    public ResponseEntity<ResponseDetails<LoginResponse>> login(@RequestBody LoginRequest loginRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        var token = authService.handleUserLogin(loginRequest, httpServletRequest, httpServletResponse);
        var loginResponse = new LoginResponse("Welcome", token);
        return ResponseEntity
                .ok()
                .body(new ResponseDetails<>(LocalDateTime.now() ,
                        "Login Successful",
                        HttpStatus.OK.toString(), loginResponse)
                        );
    }
    @PostMapping("/login/google")
    public ResponseEntity<ResponseDetails<String>> getGoogleLoginUrl() {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("google");
        String authorizationUrl = registration.getRedirectUri();
        var response = new ResponseDetails<>("redirect url", HttpStatus.OK.toString(), authorizationUrl);
        return ResponseEntity.ok(response);
    }
}
