package genum.dataset.model;

import org.springframework.data.annotation.Id;

import genum.dataset.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Datasets {

    @Id
    private String id;

    private String uploadFile;

    private Visibility visibility;
    
    private String title;

    private String fileSize;

    private int downloads;

    // TODO
    // Licence
}
