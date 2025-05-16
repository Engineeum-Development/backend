package genum.learn.model;

import genum.learn.enums.VideoUploadStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
@Getter
public class VideoUploadStatusModel {

    @Id
    private String id;

    private final String videoId;
    @Setter
    private VideoUploadStatus videoUploadStatus;
    @Setter
    private String videoUploadUrl;

    public VideoUploadStatusModel(String videoId, VideoUploadStatus videoUploadStatus) {
        this.videoId = videoId;
        this.videoUploadStatus = videoUploadStatus;
    }
}
