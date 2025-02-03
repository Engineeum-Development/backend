package genum.dataset.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import genum.dataset.model.Dataset;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetRepository extends MongoRepository<Dataset, String>{
    Optional<Dataset> getDatasetByDatasetID(String id);
    void deleteByDatasetID(String datasetID);

    boolean existsByDatasetID(String datasetID);

    List<Dataset> findTop50ByOrderByDownloadsDesc();
}
