package genum.genumUser.security;

import genum.shared.security.exception.LoginFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.concurrent.TimeUnit;


public class GenumAuthenticationProvider extends DaoAuthenticationProvider {

    @Value("${auth.maxAttempts}")
    private int MAX_ATTEMPTS;
    @Value("${auth.lockoutDuration_minutes}")
    private int LOCKOUT_DURATION_MINUTES;
    private final RedisTemplate<String, Object> redisTemplate;

    public GenumAuthenticationProvider(PasswordEncoder passwordEncoder, RedisTemplate<String, Object> redisTemplate) {
        super(passwordEncoder);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws LoginFailedException {
        if (isAccountLocked((String) authentication.getPrincipal()))
            throw new LockedException("Your account is currently locked due to too many failed attempts. Please try again in " + getRemainingLockTime((String) authentication.getPrincipal()) + " minutes");
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) authentication;
        try {
            authenticationToken = (UsernamePasswordAuthenticationToken) super.authenticate(authentication);
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
        redisTemplate.delete(attemptKey);
    }
    private int incrementFailedAttempt(String email) {
        String attemptKey = getAttemptKey(email);
        Long currentAttempts = redisTemplate.opsForValue().increment(attemptKey);

        if (currentAttempts != null && currentAttempts == 1L) {
            redisTemplate.expire(attemptKey, 24, TimeUnit.HOURS);
        }
        return currentAttempts != null ? currentAttempts.intValue() : 1;
    }
    private int getFailedAttempts(String email) {
        String attemptKeys = getAttemptKey(email);
        Object attempts = redisTemplate.opsForValue().get(attemptKeys);
        return attempts != null ? (int) attempts : 0;
    }
    private long getRemainingLockTime(String email) {
        String lockKey = getLockKey(email);
        Long expiry = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
        return expiry != null ? expiry : 0L;
    }
    private boolean isAccountLocked(String email) {
        String lockKey = getLockKey(email);
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    private void lockAccount(String email) {
        String lockKey = getLockKey(email);
        redisTemplate.opsForValue().set(lockKey, Instant.now().toString());
        redisTemplate.expire(lockKey, LOCKOUT_DURATION_MINUTES, TimeUnit.MINUTES);
    }

    private String getAttemptKey(String email) {
        return "auth:attempt:" + email;
    }
    private String getLockKey(String email) {
        return "auth:locked:" + email;
    }
}
