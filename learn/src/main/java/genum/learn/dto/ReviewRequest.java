package genum.learn.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record ReviewRequest(
                            @NotEmpty @NotBlank String lessonId,
                            @NotBlank(message = "Review comment must not contain only whitespace characters")
                            @NotEmpty(message = "Review comment must not be empty") String comment,
                            @Max(message = "Rating cannot be greater than 5", value = 5L) int rating) {
}
