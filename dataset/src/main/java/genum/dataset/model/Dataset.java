package genum.dataset.model;

import genum.dataset.domain.DatasetMetadata;
import genum.dataset.domain.DatasetType;
import org.springframework.data.annotation.Id;

import genum.dataset.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Dataset implements Serializable {

    @Id
    private String id;
    private String datasetID;
    private String datasetName;
    private String uploader;
    private String description;
    private String uploadFileUrl;
    private Visibility visibility;
    private List<String> tags;
    private DatasetType datasetType;
    private String fileName;
    private long fileSize;
    private int downloads;
    private Set<String> usersThatLiked;

    public void addUsersThatLiked(String userId) {
        usersThatLiked.add(userId);
    }


    public DatasetMetadata toMetadata() {
       var datasetMetadata = new DatasetMetadata(
               datasetName, fileName,fileSize,datasetType,visibility
        );
       datasetMetadata.setLikes(Objects.nonNull(usersThatLiked)? usersThatLiked.size(): 0);
       return datasetMetadata;
    }

    // TODO
    // Licence
}
