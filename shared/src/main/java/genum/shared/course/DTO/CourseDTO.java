package genum.shared.course.DTO;

import java.io.Serializable;

public record CourseDTO(String referenceId, String name, String uploader, long numberOfEnrolledUsers, int price, String description, String uploadDate) implements Serializable {
    public CourseDTO(String referenceId, String name, String uploader, int price, String description, String uploadDate) {
        this(referenceId, name, uploader, 0, price, description, uploadDate);
    }
}
