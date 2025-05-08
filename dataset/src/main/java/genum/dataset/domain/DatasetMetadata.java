package genum.dataset.domain;

import genum.dataset.enums.DatasetType;
import genum.dataset.enums.Visibility;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class DatasetMetadata implements Serializable {
    private String datasetName;
    private Visibility visibility;
    private long fileSizeInKBytes;
    private String userId;
    private DatasetType contentType;
    private long likes;

    public DatasetMetadata(String datasetName,String userId,long fileSizeInBytes, DatasetType contentType, Visibility visibility) {
        this.fileSizeInKBytes = fileSizeInBytes;
        this.userId = userId;
        this.contentType = contentType;
        this.visibility = visibility;
        this.datasetName = datasetName;
    }
}
