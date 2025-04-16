package genum.shared.genumUser.exception;

public class BadRequestException extends RuntimeException{
    public static final String MESSAGE = "Invalid parameters found";
    public static final String MESSAGE_FORMATTED = "Invalid parameters found: %s";

    public BadRequestException() {
        super(MESSAGE);
    }
    public BadRequestException(String message) {
        super(MESSAGE_FORMATTED.formatted(message));
    }
}
