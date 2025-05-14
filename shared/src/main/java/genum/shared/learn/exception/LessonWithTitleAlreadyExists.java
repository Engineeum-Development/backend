package genum.shared.learn.exception;


public class LessonWithTitleAlreadyExists extends RuntimeException{
    public static final String MESSAGE = "Lesson with title %s already exists. Please, choose a different title";

    public LessonWithTitleAlreadyExists(String message) {
        super(MESSAGE.formatted(message));
    }
}
