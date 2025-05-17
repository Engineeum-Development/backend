package genum.genumUser.security;

import genum.shared.security.CustomUserDetails;
import genum.shared.security.exception.LoginFailedException;
import genum.shared.util.CacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class GenumAuthenticationProvider extends DaoAuthenticationProvider {

    @Value("${auth.maxAttempts}")
    private int MAX_ATTEMPTS;
    @Value("${auth.lockoutDuration_minutes}")
    private int LOCKOUT_DURATION_MINUTES;

    private final CacheService<String, Object> cacheService;

    public GenumAuthenticationProvider(PasswordEncoder passwordEncoder, CacheService<String, Object> cacheService) {
        super(passwordEncoder);
        this.cacheService = cacheService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws LoginFailedException {
        if (isAccountLocked((String) authentication.getPrincipal()))
            throw new LockedException("Your account is currently locked due to too many failed attempts. Please try again in " + getRemainingLockTime((String) authentication.getPrincipal()) + " minutes");
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) authentication;
        try {
            authenticationToken = (UsernamePasswordAuthenticationToken) super.authenticate(authentication);
            resetFailedAttempts(((CustomUserDetails) authenticationToken.getPrincipal()).getEmail());
            return authenticationToken;
        } catch (BadCredentialsException exception) {
            handleFailedAttempts(authenticationToken);
            return authenticationToken;
        }
    }

    private void handleFailedAttempts(Authentication authenticationToken) {
        String email = (String) authenticationToken.getPrincipal();
        if (authenticationToken.isAuthenticated()){
            resetFailedAttempts(email);
            return;
        }
        var failedAttempts = incrementFailedAttempt(email);
        var remainingAttempts = MAX_ATTEMPTS - Math.min(MAX_ATTEMPTS, failedAttempts);
        if (failedAttempts >= MAX_ATTEMPTS) {
            lockAccount(email);
            throw new LoginFailedException("Your account has been locked due to too many failed attempts. Please try again in " + getRemainingLockTime(email) + " minutes");
        }
        throw new LoginFailedException("You have " + remainingAttempts + " attempts left");
    }

    private void resetFailedAttempts(String email) {
        String attemptKey = getAttemptKey(email);
        cacheService.evict(attemptKey);
    }
    private long incrementFailedAttempt(String email) {
        String attemptKey = getAttemptKey(email);
        AtomicInteger currentAttempts = new AtomicInteger(cacheService.get(attemptKey) != null?(Integer) cacheService.get(attemptKey): 0);
        cacheService.put(attemptKey, currentAttempts.incrementAndGet());
        return currentAttempts.intValue();
    }
    private int getFailedAttempts(String email) {
        String attemptKeys = getAttemptKey(email);
        return (int) cacheService.get(attemptKeys);
    }
    private long getRemainingLockTime(String email) {
        String lockKey = getLockKey(email);
        Long expiry = cacheService.getRemainingLockTime(lockKey, TimeUnit.MINUTES);
        return expiry != null ? expiry : 0L;
    }
    private boolean isAccountLocked(String email) {
        String lockKey = getLockKey(email);
        return cacheService.hasKey(lockKey);
    }

    private void lockAccount(String email) {
        String lockKey = getLockKey(email);
        cacheService.put(lockKey, Instant.now().toString());
    }

    private String getAttemptKey(String email) {
        return "auth:attempt:" + email;
    }
    private String getLockKey(String email) {
        return "auth:locked:" + email;
    }
}
