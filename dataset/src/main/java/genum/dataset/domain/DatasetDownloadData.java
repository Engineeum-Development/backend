package genum.dataset.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@AllArgsConstructor
@Getter
public class DatasetDownloadData {
    private DatasetMetadata datasetMetadata;
    private MultipartFile file;
}
