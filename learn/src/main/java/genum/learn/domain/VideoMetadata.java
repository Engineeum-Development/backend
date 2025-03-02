package genum.learn.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoMetadata {
    private String videoId;
    private String title;
    private String description;
    private long fileSizeInBytes;
    private String originalFilename;
}
