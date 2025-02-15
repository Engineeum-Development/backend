package genum.dataset.domain;

import genum.dataset.enums.Visibility;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DatasetMetadata {
    private String datasetId;
    private String description;
    private List<String> tags;
    private Visibility visibility;
    private long fileSizeInKBytes;
    private String originalFilename;
    private DatasetType contentType;
    private long likes;

    public DatasetMetadata(String description, List<String> tags,  String originalFilename,long fileSizeInBytes, DatasetType contentType, Visibility visibility) {
        this.description = description;
        this.tags = tags;
        this.fileSizeInKBytes = fileSizeInBytes;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.visibility = visibility;
    }
    public DatasetMetadata(String datasetId, String description, List<String> tags, String originalFilename, long fileSizeInBytes, DatasetType contentType, Visibility visibility) {
        this.description = description;
        this.tags = tags;
        this.fileSizeInKBytes = fileSizeInBytes;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.datasetId = datasetId;
        this.visibility = visibility;
    }
}
