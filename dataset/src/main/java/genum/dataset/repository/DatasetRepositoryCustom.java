package genum.dataset.repository;

import genum.dataset.DTO.DatasetDTO;

import java.util.Optional;

public interface DatasetRepositoryCustom {

    Optional<DatasetDTO> findDatasetDTObyDatasetID(String id);
}
