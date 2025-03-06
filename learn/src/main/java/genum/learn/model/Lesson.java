package genum.learn.model;

import genum.learn.dto.LessonDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Document
@Getter
public class Lesson {
    @MongoId
    private String id;
    @Setter
    private String referenceId;
    @Setter
    private String title;
    @Setter
    private String description;
    @Setter
    private String content; //This is in markdown
    @Setter
    private String courseId;
    private String exerciseId;
    private String nextLessonId;
    @Setter
    private Set<String> readIds;

    public Lesson(String title, String description, String content, String courseId) {
        this.referenceId = UUID.randomUUID().toString();
        this.title = title;
        this.courseId = courseId;
        this.description = description;
        this.content = content;
        this.readIds = new TreeSet<>();
    }

    public void addToReadIds(String userId) {
        readIds.add(userId);
    }

    public LessonDTO toLessonDTO() {
        return new LessonDTO(this.referenceId, this.title, this.description, this.content, String.valueOf(this.readIds.size()));
    }
}
