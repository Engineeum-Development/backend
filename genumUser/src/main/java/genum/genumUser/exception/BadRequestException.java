package genum.genumUser.exception;

public class BadRequestException extends RuntimeException{
    public static final String MESSAGE = "Invalid parameters found";

    public BadRequestException() {
        super(MESSAGE);
    }
}
