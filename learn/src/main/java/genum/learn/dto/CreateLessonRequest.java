package genum.learn.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateLessonRequest(@NotBlank String courseId, @NotBlank String title,@NotBlank String description,@NotBlank String content) {
}
