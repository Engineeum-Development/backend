package genum.shared.course.exception;

public class CourseNotFoundException extends RuntimeException {

    private static final String MESSAGE = "This product no longer exists";

    public CourseNotFoundException() {
        super(MESSAGE);
    }
}
