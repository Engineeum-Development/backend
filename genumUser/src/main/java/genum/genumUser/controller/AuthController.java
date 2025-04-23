package genum.genumUser.controller;

import genum.genumUser.service.AuthService;
import genum.shared.DTO.request.LoginRequest;
import genum.shared.DTO.response.ResponseDetails;
import genum.shared.security.LoginResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDetails<LoginResponse>> login(@RequestBody LoginRequest loginRequest, HttpServletResponse httpServletResponse) {

        var token = authService.handleUserLogin(loginRequest, httpServletResponse);
        var loginResponse = new LoginResponse("Welcome", token);
        return ResponseEntity
                .ok()
                .body(new ResponseDetails<>(LocalDateTime.now(),
                        "Login Successful",
                        HttpStatus.OK.toString(),
                        loginResponse)
                );
    }


    //The endpoint for Google Oauth login is {BaseUrl}/oauth2/authorization/google
}
