package genum.shared.genumUser.exception;

public class UserAlreadyExistsException extends RuntimeException{
    public static final String MESSAGE= "username already taken, use another one";

    public UserAlreadyExistsException() {
        super(MESSAGE);
    }
}
