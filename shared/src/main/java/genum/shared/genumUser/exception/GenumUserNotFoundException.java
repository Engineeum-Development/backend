package genum.shared.genumUser.exception;

public class GenumUserNotFoundException extends RuntimeException{
    public static final String MESSAGE = "User was not found please sign up";

    public GenumUserNotFoundException() {
        super(MESSAGE);
    }
}
