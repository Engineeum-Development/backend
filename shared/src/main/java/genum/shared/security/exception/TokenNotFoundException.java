package genum.shared.security.exception;

public class TokenNotFoundException extends RuntimeException{

    public static final String MESSAGE = "No jwt token found for path: ";

    public TokenNotFoundException(String path) {
        super(MESSAGE + path);
    }
}
