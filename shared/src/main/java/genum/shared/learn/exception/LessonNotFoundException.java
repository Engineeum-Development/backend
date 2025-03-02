package genum.shared.learn.exception;

public class LessonNotFoundException extends RuntimeException{
    public static final String MESSAGE = "No lesson with that id was found";

    public LessonNotFoundException() {
        super(MESSAGE);
    }
}
