package genum.dataset.domain;

import genum.dataset.enums.Visibility;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class DatasetMetadata implements Serializable {
    private String datasetName;
    private Visibility visibility;
    private long fileSizeInKBytes;
    private String originalFilename;
    private DatasetType contentType;
    private long likes;

    public DatasetMetadata(String datasetName,String originalFilename,long fileSizeInBytes, DatasetType contentType, Visibility visibility) {
        this.fileSizeInKBytes = fileSizeInBytes;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.visibility = visibility;
        this.datasetName = datasetName;
    }
}
