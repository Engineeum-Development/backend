package genum.dataset.service;

import genum.dataset.DTO.CreateDatasetDTO;
import genum.dataset.model.Datasets;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DatasetsService {
    Datasets createDataset(CreateDatasetDTO createDatasetDTO);

    Datasets getDatasetById(String id);

    Page<Datasets> getAllDatasets(Pageable pageable);

    Datasets updateDataset(String id, Datasets updatedDataset);

    void deleteDataset(String id);

    List<Datasets> trending();

    void downloadDataset(String id);
}
