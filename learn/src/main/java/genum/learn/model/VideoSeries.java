package genum.learn.model;

import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Set;

@Document
@Getter
public class VideoSeries {
    @MongoId
    private String id;
    private String reference;
    private Set<String> videosAccordingToOrder;
    private String title;
    private String lessonReference;
    private String description;
    private Set<String> tags;
}
