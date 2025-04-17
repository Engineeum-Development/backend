package genum.learn.dto;

import jakarta.validation.constraints.Max;

public record ReviewRequest(String lessonId, String comment, @Max(message = "Rating cannot be greater than 5", value = 5L) int rating) {
}
