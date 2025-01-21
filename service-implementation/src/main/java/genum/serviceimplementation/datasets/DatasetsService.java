package genum.serviceimplementation.datasets;

import genum.dataset.DTO.CreateDatasetDTO;
import genum.dataset.model.Datasets;

import java.util.List;

public interface DatasetsService {
    Datasets createDataset(CreateDatasetDTO createDatasetDTO);

    Datasets getDatasetById(String id);

    List<Datasets> getAllDatasets();

    Datasets updateDataset(String id, Datasets updatedDataset);

    void deleteDataset(String id);

    List<Datasets> trending();

    void downloadDataset(String id);
}
