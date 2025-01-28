package genum.app.exception;

import genum.genumUser.exception.BadRequestException;
import genum.genumUser.security.exception.TokenNotFoundException;
import genum.shared.DTO.response.ResponseDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler{
    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ResponseDetails<String>> handleTokenNotFoundException(TokenNotFoundException tokenNotFoundException) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDetails<>(LocalDateTime.now(), tokenNotFoundException.getMessage(), HttpStatus.UNAUTHORIZED.toString()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseDetails<String>> handleTokenNotFoundException(BadRequestException badRequestException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDetails<>(LocalDateTime.now(), badRequestException.getMessage(), HttpStatus.BAD_REQUEST.toString()));
    }
}
