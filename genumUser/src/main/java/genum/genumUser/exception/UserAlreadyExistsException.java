package genum.genumUser.exception;

public class UserAlreadyExistsException extends RuntimeException{
    public static final String MESSAGE= "Invalid username try another one";

    public UserAlreadyExistsException() {
        super(MESSAGE);
    }
}
