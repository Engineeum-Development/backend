package genum.dataset.model;

import genum.dataset.domain.DatasetMetadata;
import genum.dataset.domain.DatasetType;
import org.springframework.data.annotation.Id;

import genum.dataset.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Dataset {

    @Id
    private String id;
    private String datasetID;
    private String uploader;
    private String description;
    private String uploadFileUrl;
    private Visibility visibility;
    private List<String> tags;
    private DatasetType datasetType;
    private String title;
    private long fileSize;
    private int downloads;
    private Set<String> usersThatLiked;

    public void addUsersThatLiked(String userId) {
        usersThatLiked.add(userId);
    }


    public DatasetMetadata toMetadata() {
       var datasetMetadata = new DatasetMetadata(
                datasetID,
                description,
                tags,
                title,
                fileSize/1000,
                datasetType,
                visibility
        );

       datasetMetadata.setLikes(usersThatLiked.size());
       return datasetMetadata;
    }

    // TODO
    // Licence
}
