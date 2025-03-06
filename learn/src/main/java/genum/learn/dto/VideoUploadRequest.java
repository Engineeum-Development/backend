package genum.learn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record VideoUploadRequest(String lessonId, String videoSeriesId, @NotBlank String description, @NotBlank String title,  Set<String> tags) {}
