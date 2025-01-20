package genum.dataset.DTO;

import genum.dataset.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateDatasetDTO {

    private String uploadFile;

    private Visibility visibility;

    private String title;

    private String fileSize;
}
