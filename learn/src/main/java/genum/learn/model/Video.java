package genum.learn.model;

import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
@Getter
public class Video {
    @MongoId
    private String id;
    private String videoId;
    private String seriesReference;
    private int videoPositionInSeries;
    private String description;
    private String title;
    private String uploadVideoFileUrl;
}
