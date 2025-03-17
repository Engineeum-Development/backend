package genum.shared.security.exception;

import org.springframework.security.core.AuthenticationException;

public class LoginFailedException extends AuthenticationException {
    public static final String MESSAGE = "Login failed because of invalid credentials";
    public LoginFailedException() {
        super(MESSAGE);
    }
    public LoginFailedException(String message) {
        super(MESSAGE + ": " + message);
    }
}
