package genum.learn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateLessonRequest(@NotBlank @NotEmpty String courseId,
                                  @NotBlank @NotEmpty String title,
                                  @NotBlank @NotEmpty String description,
                                  @NotBlank @NotEmpty String content) {
}
