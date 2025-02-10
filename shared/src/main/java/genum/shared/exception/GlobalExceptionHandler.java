package genum.shared.exception;


import genum.shared.DTO.response.ResponseDetails;
import genum.shared.genumUser.exception.BadRequestException;
import genum.shared.security.exception.InvalidTokenException;
import genum.shared.security.exception.TokenNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;



import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler{
    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ResponseDetails<String>> handleTokenNotFoundException(TokenNotFoundException tokenNotFoundException) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDetails<>(LocalDateTime.now(), tokenNotFoundException.getMessage(), HttpStatus.UNAUTHORIZED.toString()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleGeneralException(RuntimeException ex) {
        log.error("And error occurred");
        log.trace("Error trace", ex);
        return new ResponseEntity<>("An error occurred on our end, and we are hard at work to fix it" , HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<String> handleLockedException(LockedException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<String> handleLockedException(DisabledException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ResponseDetails<String>> handleTokenInvalidTokenException(InvalidTokenException invalidTokenException) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDetails<>(LocalDateTime.now(), invalidTokenException.getMessage(), HttpStatus.UNAUTHORIZED.toString()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseDetails<String>> handleTokenNotFoundException(BadRequestException badRequestException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDetails<>(LocalDateTime.now(), badRequestException.getMessage(), HttpStatus.BAD_REQUEST.toString()));
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseDetails<String>> handleBadCredentialsException(BadCredentialsException badCredentialsException) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDetails<>(LocalDateTime.now(), "invalid credentials, Please try again with correct credentials", HttpStatus.UNAUTHORIZED.toString()));
    }
}
