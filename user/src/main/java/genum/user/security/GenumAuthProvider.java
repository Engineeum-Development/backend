package genum.user.security;

import genum.user.repository.GenumUserRepository;
import genum.shared.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class GenumAuthProvider implements AuthenticationProvider {

    private final GenumUserRepository genumUserRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var userAuth = (GenumAuthentication) authentication;
        var user = genumUserRepository.findByEmail(((GenumAuthentication) authentication).getEmail());
        if (Objects.nonNull(user)) {

            if (passwordEncoder.matches(userAuth.getPassword(), user.getCustomUserDetails().getPassword())) {
                validAccount.accept(user.getCustomUserDetails());
                var userDetails = user.getCustomUserDetails();
                userDetails.setLastLogin(LocalDateTime.now());
                user.setCustomUserDetails(userDetails);
                genumUserRepository.save(user);
                return GenumAuthentication.authenticated(userDetails, userDetails.getAuthorities());
            }
            else throw new BadCredentialsException("Invalid email or password");
        }
        else throw new BadCredentialsException("Invalid email or password");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return GenumAuthentication.class.isAssignableFrom(authentication);
    }

    private final Consumer<CustomUserDetails> validAccount = userDetails -> {
        if (!userDetails.isAccountNonLocked()) {
            throw new LockedException("Your account is currently locked");
        }
        if (!userDetails.isAccountNonExpired()) {
            throw new LockedException("Your account has expired");
        }
        if (!userDetails.isCredentialsNonExpired()) {
            throw new LockedException("Your credentials are expired");
        }
        if (!userDetails.isEnabled()) {
            throw new LockedException("Your account is currently disabled");
        }
    };
}
