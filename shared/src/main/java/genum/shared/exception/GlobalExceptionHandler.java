package genum.shared.exception;


import genum.shared.DTO.response.ResponseDetails;
import genum.shared.dataset.exception.DatasetNotFoundException;
import genum.shared.genumUser.exception.BadRequestException;
import genum.shared.genumUser.exception.GenumUserNotFoundException;
import genum.shared.genumUser.exception.OTTNotFoundException;
import genum.shared.genumUser.exception.UserAlreadyExistsException;
import genum.shared.learn.exception.LessonNotFoundException;
import genum.shared.learn.exception.LessonWithTitleAlreadyExists;
import genum.shared.payment.exception.PaymentNotFoundException;
import genum.shared.course.exception.CourseNotFoundException;
import genum.shared.security.exception.InvalidTokenException;
import genum.shared.security.exception.LoginFailedException;
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


import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler{
    private record FieldError(String field, String error){}
    public record ValidationErrorResponse(String message, List<FieldError> fieldErrors){}

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ResponseDetails<String>> handleTokenNotFoundException(TokenNotFoundException tokenNotFoundException) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDetails<>(tokenNotFoundException.getMessage(), HttpStatus.UNAUTHORIZED.toString()));
    }
    @ExceptionHandler(LessonWithTitleAlreadyExists.class)
    public ResponseEntity<ResponseDetails<String>> handleTokenNotFoundException(LessonWithTitleAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ResponseDetails<>(ex.getMessage(), HttpStatus.CONFLICT.toString()));
    }
    @ExceptionHandler(OTTNotFoundException.class)
    public ResponseEntity<ResponseDetails<String>> handleOTTNotFoundException(OTTNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDetails<>(ex.getMessage(), HttpStatus.NOT_FOUND.toString()));
    }
    @ExceptionHandler(GenumUserNotFoundException.class)
    public ResponseEntity<ResponseDetails<String>> handleGenumUserNotFoundException(GenumUserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDetails<>(ex.getMessage(),HttpStatus.NOT_FOUND.toString()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleGeneralException(RuntimeException ex) {
        log.error("And error occurred {} {}",ex.getClass(), ex.getMessage());
        log.error("Error trace", ex);
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
                .body(new ResponseDetails<>(invalidTokenException.getMessage(), HttpStatus.UNAUTHORIZED.toString()));
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseDetails<String>> handleBadCredentialsException(BadCredentialsException badCredentialsException) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDetails<>("Invalid credentials: Please try again with correct credentials, %s".formatted(badCredentialsException.getMessage()), HttpStatus.UNAUTHORIZED.toString()));
    }
    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ResponseDetails<String>> handleLoginFailedException(LoginFailedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDetails<>(ex.getMessage(), HttpStatus.UNAUTHORIZED.toString()));
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
    public ResponseEntity<ResponseDetails<ValidationErrorResponse>> handleMethodValidationException(MethodArgumentNotValidException validationException) {
        var bindingResult = validationException.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors().stream()
                .map(fieldError -> new FieldError(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(
                new ResponseDetails<>("Invalid fields", HttpStatus.BAD_REQUEST.toString(), new ValidationErrorResponse("Validation failed for one or more fields", fieldErrors))
        );
    }
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ResponseDetails<String>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ResponseDetails<>(ex.getMessage(), HttpStatus.CONFLICT.toString()));
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseDetails<String>> handleBadRequestException(BadRequestException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDetails<>(ex.getMessage(), HttpStatus.BAD_REQUEST.toString()));
    }

    @ExceptionHandler(DatasetNotFoundException.class)
    public ResponseEntity<ResponseDetails<String>> handleDatasetNotFoundException(DatasetNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ResponseDetails<>(ex.getMessage(), HttpStatus.NOT_FOUND.toString()));
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ResponseDetails<String>> handleProductNotFoundException(CourseNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ResponseDetails<>(ex.getMessage(), HttpStatus.NOT_FOUND.toString()));
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ResponseDetails<String>> handleProductNotFoundException(PaymentNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ResponseDetails<>(ex.getMessage(), HttpStatus.NOT_FOUND.toString()));
    }

    @ExceptionHandler(LessonNotFoundException.class)
    public ResponseEntity<ResponseDetails<String>> handleLessonNotFoundException(LessonNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ResponseDetails<>(ex.getMessage(), HttpStatus.NOT_FOUND.toString()));
    }
}
