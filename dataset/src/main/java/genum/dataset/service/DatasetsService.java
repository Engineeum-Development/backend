package genum.dataset.service;

import genum.dataset.DTO.CreateDatasetDTO;
import genum.dataset.domain.DatasetMetadata;
import genum.dataset.model.Dataset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DatasetsService {
    // Returns the ID of the dataset file
    String createDataset(CreateDatasetDTO createDatasetDTO, MultipartFile file) throws IOException;

    DatasetMetadata getDatasetMetadataById(String id);

    Dataset getDatasetById(String id);

    Page<DatasetMetadata> getAllDatasets(Pageable pageable);

    DatasetMetadata updateDatasetMetadata(String id, DatasetMetadata metadata);

    Dataset updateDataset(String id, DatasetMetadata updatedDataset);

    void deleteDataset(String id);

    Page<DatasetMetadata> trending(Pageable pageable);

    String downloadDataset(String id);

    void likeDataset(String id);
}
