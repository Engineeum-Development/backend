package genum.learn.dto;

import jakarta.validation.constraints.*;

public record CourseUploadRequest(
        @NotBlank @NotEmpty String name,
        @NotBlank @NotEmpty String description,
        @Min(5) @Max(150000)int price
) {
}
