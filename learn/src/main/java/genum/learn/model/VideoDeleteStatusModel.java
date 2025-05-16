package genum.learn.model;

import genum.learn.enums.VideoDeleteStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
@Getter
public class VideoDeleteStatusModel {
    @Id
    private String id;
    private final String videoId;
    private final VideoDeleteStatus videoDeleteStatus;

    public VideoDeleteStatusModel(String videoId, VideoDeleteStatus videoDeleteStatus) {
        this.videoId = videoId;
        this.videoDeleteStatus = videoDeleteStatus;
    }
}
