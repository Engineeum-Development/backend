package genum.learn.model;

import genum.learn.enums.VideoDeleteStatus;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
public class VideoDeleteStatusModel {
    private String id;
    private final String videoId;
    private final VideoDeleteStatus videoDeleteStatus;

    public VideoDeleteStatusModel(String videoId, VideoDeleteStatus videoDeleteStatus) {
        this.videoId = videoId;
        this.videoDeleteStatus = videoDeleteStatus;
    }
}
