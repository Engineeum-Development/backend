package genum.shared.learn.exception;

public class VideoNotFoundException extends RuntimeException{

    public static final String MESSAGE = "This video does not exist";

    public VideoNotFoundException() {
        super(MESSAGE);
    }
}
