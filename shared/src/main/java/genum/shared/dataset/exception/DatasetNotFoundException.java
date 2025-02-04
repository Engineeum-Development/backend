package genum.shared.dataset.exception;

public class DatasetNotFoundException extends RuntimeException{
    public static final String MESSAGE = "Dataset with %s is not found";

    public DatasetNotFoundException(String message) {

        super(MESSAGE.formatted(message));
    }
}
