package genum.learn.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateCourseRequest(
        @NotBlank String name,
        @NotBlank String description,
        @Positive @Min(5) int price
) {
}
