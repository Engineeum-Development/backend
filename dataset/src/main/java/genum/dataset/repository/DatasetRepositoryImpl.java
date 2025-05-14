package genum.dataset.repository;

import genum.dataset.DTO.DatasetDTO;
import genum.dataset.model.Dataset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
@Slf4j
public class DatasetRepositoryImpl implements DatasetRepositoryCustom {

    private final MongoTemplate mongoTemplate;
    @Override
    public Optional<DatasetDTO> findDatasetDTObyDatasetID(String id) {
        MatchOperation matchOperation = Aggregation
                .match(Criteria.where("datasetID").is(id));
        ProjectionOperation projectionOperation = Aggregation
                .project()
                .and("datasetID").as("datasetId")
                .and("datasetName").as("name")
                .and("datasetSubtitle").as("subtitle")
                .and("datasetThumbnailImageUrl").as("thumbnailURL")
                .and("uploadFileUrl").as("fileDownloadURL")
                .and("tags").as("tags")
                .and("pendingActions").as("pendingActions")
                .and("datasetType").as("datasetFormat")
                .and("downloads").as("downloads")
                .and("license").as("license")
                .and("doiCitation").as("doiCitation")
                .and("provenance").as("provenance")
                .and("collaborators").as("collaborators")
                .and("usersThatUpvote").size().as("upvotes")
                .and("authors").as("authors")
                .and("coverage").as("coverage")
                ;

        TypedAggregation<Dataset> aggregation = Aggregation.newAggregation(Dataset.class, matchOperation, projectionOperation);
        AggregationResults<DatasetDTO> results = mongoTemplate.aggregate(aggregation, "dataset", DatasetDTO.class);

        log.info("result {}", results.getRawResults().toJson());

        return Optional.ofNullable(results.getUniqueMappedResult());
    }
}
