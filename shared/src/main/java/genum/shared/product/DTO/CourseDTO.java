package genum.shared.product.DTO;

public record CourseDTO(String referenceId, String name,String uploader, long numberOfEnrolledUsers, int price, String description, String uploadDate) {
    public CourseDTO(String referenceId, String name, String uploader, int price, String description, String uploadDate) {
        this(referenceId, name, uploader, 0, price, description, uploadDate);
    }
}
