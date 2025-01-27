package genum.genumUser.security.jwt;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import genum.genumUser.security.GenumAuthentication;
import genum.genumUser.security.constant.SecurityConstants;
import genum.shared.security.LoginResponse;
import genum.shared.DTO.request.LoginRequest;
import genum.shared.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
public class LoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public LoginAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        super(new AntPathRequestMatcher(SecurityConstants.LOGIN_PATH, HttpMethod.POST.name()), authenticationManager);
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        setFilterProcessesUrl("/api/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.trace("Entering attempt authentication");
        try {
            LoginRequest credential = new ObjectMapper()
                    .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)
                    .readValue(request.getInputStream(), LoginRequest.class);
            return authenticationManager.authenticate(GenumAuthentication.unAuthenticated(credential.email(), credential.password()));
        } catch (IOException exception) {
            log.trace("Authentication failed because of IOException");
            throw new RuntimeException("Authentication failed");
        } catch (AuthenticationException exception) {
            log.trace("Authentication failed because of Invalid email or password");
            throw new BadCredentialsException("Invalid email or password");
        }
    }


    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        log.trace("Successful login");
        ObjectMapper mapper = new ObjectMapper();
        LoginResponse loginResponse = new LoginResponse(LocalDateTime.now(), "Login successful");

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jwtUtils.addHeader(response, (CustomUserDetails) authResult.getPrincipal());
        try (var out = response.getOutputStream()) {
            mapper.writeValue(out, loginResponse);
            log.trace("Output written");
        }
    }
}