package genum.genumUser.security.jwt;

import genum.genumUser.security.constant.SecurityConstants;
import genum.genumUser.security.domain.TokenData;
import genum.genumUser.repository.GenumUserRepository;
import genum.shared.constant.Role;
import genum.shared.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class JwtUtils {

    private final GenumUserRepository genumUserRepository;

    public JwtUtils(GenumUserRepository genumUserRepository) {
        this.genumUserRepository = genumUserRepository;
    }

    private final Supplier<SecretKey> keySupplier = () -> Keys
            .hmacShaKeyFor(
                    Decoders.BASE64.decode(SecurityConstants.SECRET)
            );

    private final Function<String, Claims> claimsFunction = token ->
            Jwts.parser()
                    .verifyWith(keySupplier.get())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
    private final Function<String, String> subject = token -> getClaimsValue(token, Claims::getSubject);

    private final Function<HttpServletRequest, Optional<String>> extractToken = httpServletRequest -> {
        var jwtToken = httpServletRequest.getHeader("Authorization");
        if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
            jwtToken = jwtToken.substring(7);
            return Optional.of(jwtToken);
        }
        return Optional.empty();
    };

    private final BiConsumer<HttpServletResponse, CustomUserDetails> addTokenToHeaderFunction = (response, user) -> {
        var accessToken = createToken(user);
        response.addHeader("Authorization", String.format("Bearer %s", accessToken));
    };

    private final Supplier<JwtBuilder> builder = () ->
            Jwts.builder()
                    .header().add(Map.of(SecurityConstants.TYPE, SecurityConstants.JWT_TYPE))
                    .and()
                    .audience().add("genum")
                    .and()
                    .id(String.valueOf(UUID.randomUUID()))
                    .issuedAt(Date.from(Instant.now()))
                    .notBefore(new Date())
                    .signWith(keySupplier.get(), Jwts.SIG.HS512);
    private final Function<CustomUserDetails, String> buildToken = (user) ->
            builder.get()
                    .subject(user.getEmail())
                    .claim(SecurityConstants.ROLE, Role.USER)
                    .expiration(Date.from(Instant.now().plusSeconds(SecurityConstants.EXPIRATION_TIME)))
                    .compact();

    public <T> T getClaimsValue(String token, Function<Claims, T> claims) {
        return claimsFunction.andThen(claims).apply(token);
    }

    public String createToken(CustomUserDetails userDetails) {
        return buildToken.apply(userDetails);
    }
    public Optional<String> extractToken(HttpServletRequest httpServletRequest) {
        return extractToken.apply(httpServletRequest);
    }

    public void addHeader(HttpServletResponse response, CustomUserDetails user) {
        addTokenToHeaderFunction.accept(response,user);
    }

    public <T> T getTokenData(String token, Function<TokenData, T> tokenFunction) {
        var userDetails = genumUserRepository.findByCustomUserDetailsEmail(subject.apply(token)).getCustomUserDetails();
        return tokenFunction.apply(
                TokenData.builder()
                        .valid(
                                Objects.equals(userDetails.getEmail(), claimsFunction.apply(token).getSubject())
                        )
                        .expired(Instant.now().isAfter(getClaimsValue(token, Claims::getExpiration).toInstant()))
                        .grantedAuthorities(List.of(new SimpleGrantedAuthority((String) claimsFunction.apply(token).get(SecurityConstants.ROLE))))
                        .claims(claimsFunction.apply(token))
                        .user(userDetails)
                        .build()
        );
    }

    public boolean validateToken(HttpServletRequest request) {
        var token = extractToken(request);
        if (token.isPresent()) {
            return getTokenData(token.get(), TokenData::isValid) ;
        }
        return false;
    }
}

