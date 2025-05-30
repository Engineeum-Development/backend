package genum.genumUser.security.jwt;


import genum.genumUser.repository.GenumUserRepository;
import genum.genumUser.security.domain.TokenData;
import genum.shared.genumUser.exception.GenumUserNotFoundException;
import genum.shared.security.exception.InvalidTokenException;
import genum.shared.security.exception.TokenNotFoundException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.awt.print.Pageable;
import java.io.IOException;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Slf4j
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final GenumUserRepository genumUserRepository;
    //White-listed paths
    private static final Pattern ROOT_PATHS = Pattern.compile("^/$");
    private static final Pattern ACTUATOR_PATHS = Pattern.compile("^/(actuator|favicon.ico)(/.*)?");
    public static final Pattern USER_PATHS = Pattern.compile("^/api/user/(create|waiting-list|confirm-token)");
    public static final Pattern AUTH_PATHS = Pattern.compile("^/api/auth/.*|^/login/.*|^/oauth2/.*");
    public static final Pattern DATASET_PATHS = Pattern.compile("^/api/dataset/(all|all/.*|trending|tag|license|download/.*)");
    public static final Pattern WEBSOCKET_PATHS = Pattern.compile("/ws|/ws/.*");
    public static final Pattern PAYMENT_PATHS = Pattern.compile("/api/payment/(paystack|flutterwave)/webhook");


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        if ( AUTH_PATHS.matcher(requestUri).matches() ||
                ROOT_PATHS.matcher(requestUri).matches() ||
                USER_PATHS.matcher(requestUri).matches() ||
                ACTUATOR_PATHS.matcher(requestUri).matches() ||
                DATASET_PATHS.matcher(requestUri).matches() ||
                WEBSOCKET_PATHS.matcher(requestUri).matches() ||
                PAYMENT_PATHS.matcher(requestUri).matches()
        ) {
            filterChain.doFilter(request,response);
        } else {
            var optionalToken = jwtUtils.extractToken(request);

            if (optionalToken.isEmpty()) {
                throw new TokenNotFoundException(requestUri);
            } else {
                var jwtToken = optionalToken.get();
                var referenceId = jwtUtils.getClaimsValue(jwtToken, Claims::getSubject);
                log.info("User Authenticated = {}", referenceId);
                if (referenceId != null || SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userDetails = genumUserRepository
                            .findByCustomUserDetails_UserReferenceId(referenceId)
                            .orElseThrow(GenumUserNotFoundException::new).getCustomUserDetails();
                    if (jwtUtils.validateToken(request)) {
                        var authorities = jwtUtils.getTokenData(jwtToken, TokenData::getGrantedAuthorities);
                        var genumAuthenticationToken = UsernamePasswordAuthenticationToken.authenticated(userDetails, "[PROTECTED]", authorities);
                        genumAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(genumAuthenticationToken);
                    } else {
                        throw new InvalidTokenException();
                    }
                } else {
                    throw new InvalidTokenException();
                }
                filterChain.doFilter(request, response);
            }

        }
    }
}