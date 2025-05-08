package genum.shared.security.exception;

public class UserNotFoundException extends RuntimeException{

    public static final String MESSAGE = "User does not exist";

    public UserNotFoundException() {
        super(MESSAGE);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
