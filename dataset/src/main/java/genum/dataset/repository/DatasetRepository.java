package genum.dataset.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import genum.dataset.model.Datasets;

@Repository
public interface DatasetRepository extends MongoRepository<Datasets, String>{
    
}
