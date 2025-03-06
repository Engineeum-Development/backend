package genum.learn.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LessonResponse(String lessonId, String title, String description, String videoSeriesId) {
}
