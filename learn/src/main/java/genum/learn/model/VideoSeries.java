package genum.learn.model;

import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Document
@Getter
public class VideoSeries {
    @MongoId
    private String id;
    private final String reference;
    private final Set<String> videosAccordingToOrder;
    private final String title;
    private final String lessonReference;
    private final String description;
    private final Set<String> tags;

    public VideoSeries(Video video, String title, String lessonReference, String description, Set<String> tags) {
        this.videosAccordingToOrder = new TreeSet<>(Collections.singleton(video.getVideoId()));
        this.reference = UUID.randomUUID().toString();
        this.title = title;
        this.lessonReference = lessonReference;
        this.description = description;
        this.tags = new TreeSet<>(tags);
    }
}
