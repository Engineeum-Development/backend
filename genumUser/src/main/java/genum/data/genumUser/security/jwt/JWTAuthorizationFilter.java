package genum.data.genumUser.security.jwt;


import genum.data.genumUser.security.GenumAuthentication;
import genum.data.genumUser.security.domain.TokenData;
import genum.data.genumUser.security.exception.InvalidTokenException;
import genum.data.genumUser.security.exception.TokenNotFoundException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

@RequiredArgsConstructor
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailService;




//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
//        String header = request.getHeader(HEADER_STRING);
//        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
//            chain.doFilter(request, response);
//            return;
//        }
//        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = getAuthentication(request);
//        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
//        chain.doFilter(request, response);
//    }
//    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
//        String token = request.getHeader(HEADER_STRING);
//        if (token != null) {
//            String user = JWT.require(Algorithm.HMAC512(SECRET.getBytes()))
//                    .build()
//                    .verify(token.replace(TOKEN_PREFIX, ""))
//                    .getSubject();
//
//            if (user != null) {
//                return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
//            }
//            return null;
//        }
//        return null;
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var optionalToken = jwtUtils.extractToken(request);

        if (optionalToken.isEmpty()){
            throw new TokenNotFoundException();
        } else {
            var jwtToken = optionalToken.get();
            var email = jwtUtils.getClaimsValue(jwtToken, Claims::getSubject);

            if (email!= null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userDetails = userDetailService.loadUserByUsername(email);
                if (jwtUtils.validateToken(request)) {
                    var authorities = jwtUtils.getTokenData(jwtToken, TokenData::getGrantedAuthorities);
                    var genumAuthenticationToken = GenumAuthentication.authenticated(userDetails, authorities);
                    genumAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(genumAuthenticationToken);
                } else {
                    throw new InvalidTokenException();
                }
            } else {
                throw new InvalidTokenException();
            }
            filterChain.doFilter(request,response);
        }



    }
}