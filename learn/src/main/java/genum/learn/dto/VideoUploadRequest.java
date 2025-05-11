package genum.learn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record VideoUploadRequest(String uploadId,
                                 String lessonId,
                                 @NotBlank @NotEmpty String description,
                                 @NotBlank @NotEmpty String title,
                                 Set<String> tags) {}
