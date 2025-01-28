package genum.shared.security.exception;

public class TokenNotFoundException extends RuntimeException{

    public static final String MESSAGE = "No jwt token found";

    public TokenNotFoundException() {
        super(MESSAGE);
    }
}
