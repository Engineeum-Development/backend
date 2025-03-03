package genum.shared.exception;


import genum.shared.DTO.response.ResponseDetails;
import genum.shared.genumUser.exception.BadRequestException;
import genum.shared.security.exception.InvalidTokenException;
import genum.shared.security.exception.TokenNotFoundException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;



import java.time.LocalDateTime;
import java.util.Map;

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
        log.error("And error occurred {} {}",ex.getClass(), ex.getMessage());
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

    @ExceptionHandler(UploadSizeLimitExceededException.class)
    public ResponseEntity<ResponseDetails<String>> handleMaxFileSizeExceeded(UploadSizeLimitExceededException uploadSizeLimitExceededException) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ResponseDetails<>(uploadSizeLimitExceededException.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE.toString()));
    }
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ResponseDetails<String>> handleValidationException(ValidationException validationException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDetails<>(validationException.getMessage(), HttpStatus.BAD_REQUEST.toString()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDetails<Map>> handleValidationException(MethodArgumentNotValidException validationException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDetails<>(validationException.getBody().getDetail(), HttpStatus.BAD_REQUEST.toString(), validationException.getBody().getProperties()));
    }
}
