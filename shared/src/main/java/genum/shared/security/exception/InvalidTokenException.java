package genum.shared.security.exception;

public class InvalidTokenException extends RuntimeException {

    public static final String MESSAGE = "Token is either incorrect or invalid";

    public InvalidTokenException() {
        super(MESSAGE);
    }
}
