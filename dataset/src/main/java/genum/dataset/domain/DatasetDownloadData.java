package genum.dataset.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.nio.file.Path;

@AllArgsConstructor
@Getter
public class DatasetDownloadData implements Serializable {
    private DatasetMetadata datasetMetadata;
    private MultipartFile file;
}
