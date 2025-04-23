package genum.dataset.repository;

import genum.dataset.model.Dataset;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetRepository extends MongoRepository<Dataset, String> {
    Optional<Dataset> getDatasetByDatasetID(String id);

    void deleteByDatasetID(String datasetID);

    boolean existsByDatasetID(String datasetID);

    List<Dataset> findTop100ByOrderByDownloadsDesc();
}
