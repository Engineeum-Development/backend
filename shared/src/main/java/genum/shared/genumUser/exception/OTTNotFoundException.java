package genum.shared.genumUser.exception;

public class OTTNotFoundException extends RuntimeException{
    public static final String MESSAGE = "OTT not valid or has expired, Try confirming email again";

    public OTTNotFoundException() {
        super(MESSAGE);
    }
}
