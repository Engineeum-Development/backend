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
    @GetMapping("/login/google")
    public ResponseEntity<ResponseDetails<String>> getGoogleLoginUrl(HttpServletRequest request, HttpServletResponse response) throws URISyntaxException {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("google");

        String state = UUID.randomUUID().toString();
        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest
                .authorizationCode()
                .clientId(registration.getClientId())
                .authorizationUri(registration.getProviderDetails().getAuthorizationUri())
                .redirectUri(registration.getRedirectUri())
                .scopes(registration.getScopes())
                .state(state)
                .build();

        auth2AuthorizationRequestRepository.saveAuthorizationRequest(authorizationRequest, request, response);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromUriString(registration.getProviderDetails().getAuthorizationUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", registration.getClientId())
                .queryParam("scope", String.join("%20", registration.getScopes()))
                .queryParam("state", state)
                .queryParam("redirect_uri", registration.getRedirectUri());
        String authorizationUrl = uriComponentsBuilder.build().toUriString();
        var responseOut = new ResponseDetails<>("Redirect Uri", HttpStatus.FOUND.toString(), authorizationUrl);

        return ResponseEntity.status(HttpStatus.OK).body(responseOut);
    }
}
