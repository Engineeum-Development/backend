package genum.learn.model;

import genum.learn.dto.VideoUploadStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
public class VideoUploadStatusModel {

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
