package genum.dataset.service;

import genum.dataset.DTO.CreateDatasetDTO;
import genum.dataset.domain.DatasetDownloadData;
import genum.dataset.domain.DatasetMetadata;
import genum.dataset.model.Dataset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DatasetsService {
    // Returns the ID of the dataset file
    String createDataset(CreateDatasetDTO createDatasetDTO, MultipartFile file);

    DatasetMetadata getDatasetMetadataById(String id);

    Dataset getDatasetById(String id);

    Page<DatasetMetadata> getAllDatasets(Pageable pageable);

    DatasetMetadata updateDataset(String id, DatasetMetadata updatedDataset);

    void deleteDataset(String id);

    List<DatasetMetadata> trending();

    DatasetDownloadData downloadDataset(String id);
}
