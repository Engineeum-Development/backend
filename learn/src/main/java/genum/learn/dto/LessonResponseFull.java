package genum.learn.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record LessonResponseFull(String lessonId, String title, String description, String content, String videoUrl, int reads) {
}
