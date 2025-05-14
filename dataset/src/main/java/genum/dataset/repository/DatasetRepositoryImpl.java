package genum.dataset.repository;

import genum.dataset.DTO.DatasetDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class DatasetRepositoryImpl implements DatasetRepositoryCustom {

    private final MongoTemplate mongoTemplate;
    @Override
    public Optional<DatasetDTO> findDatasetDTObyDatasetID(String id) {
        MatchOperation matchOperation = Aggregation
                .match(Criteria.where("datasetID").is(id)
                        .and("visibility").is("public"));
        ProjectionOperation projectionOperation = Aggregation
                .project("description",
                        "tags",
                        "pendingActions",
                        "license",
                        "doiCitation",
                        "downloads",
                        "collaborators",
                        "provenance",
                        "authors")
                .and("datasetID").as("datasetId")
                .and("datasetName").as("name")
                .and("datasetSubtitle").as("subtitle")
                .and("datasetThumbnailImageUrl").as("thumbnailURL")
                .and("uploadFileUrl").as("fileDownloadURL")
                .and("datasetType").as("datasetFormat")
                .and("usersThatUpvote").size().as("upvotes");

        Aggregation aggregation = Aggregation.newAggregation(matchOperation, projectionOperation);
        AggregationResults<DatasetDTO> results = mongoTemplate.aggregate(aggregation, "dataset", DatasetDTO.class);

        return Optional.ofNullable(results.getUniqueMappedResult());
    }
}
