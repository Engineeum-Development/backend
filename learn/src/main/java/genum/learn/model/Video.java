package genum.learn.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Document
@Getter
@Setter
public class Video {
    @Id
    private String id;
    private String videoId;
    private String lessonId;
    private String description;
    private String title;
    private String uploadVideoFileUrl;
    private Set<String> tags;

    public Video(String description, String title, String lessonId) {
        this.description = description;
        this.title = title;
        this.videoId = UUID.randomUUID().toString();
        this.lessonId = lessonId;
        tags = new HashSet<>();
    }
}
