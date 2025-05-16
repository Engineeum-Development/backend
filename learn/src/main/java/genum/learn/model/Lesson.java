package genum.learn.model;

import genum.learn.dto.LessonDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Document("lesson")
@Getter
@Setter
public class Lesson implements Serializable {
    @Id
    private String id;
    private String referenceId;
    private String title;
    private String description;
    private String content; //This is in markdown
    private String courseId;
    private Set<String> readIds;

    public Lesson(String title, String description, String content, String courseId) {
        this.referenceId = UUID.randomUUID().toString();
        this.title = title;
        this.courseId = courseId;
        this.description = description;
        this.content = content;
        this.readIds = new HashSet<>();
    }

    public void addToReadIds(String userId) {
        readIds.add(userId);
    }

    public LessonDTO toLessonDTO() {
        return new LessonDTO(this.referenceId,this.courseId, this.title, this.description, this.content, this.readIds.size());
    }
}
