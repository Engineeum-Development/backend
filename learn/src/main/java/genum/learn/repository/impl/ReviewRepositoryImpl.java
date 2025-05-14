package genum.learn.repository.impl;

import genum.learn.model.Review;
import genum.learn.repository.ReviewRepositoryCustom;
import genum.learn.repository.projection.AverageRating;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    public final MongoTemplate mongoTemplate;
    @Override
    public Optional<AverageRating> findRatingFromReviewsByCourseId(String courseId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("courseId").is(courseId));
        GroupOperation groupOperation = Aggregation.group("courseId")
                .avg("rating").as("averageRating");
        TypedAggregation<Review> aggregation = Aggregation.newAggregation(Review.class, matchOperation,groupOperation);

        AggregationResults<AverageRating> results = mongoTemplate.aggregate(aggregation, AverageRating.class);
        return Optional.ofNullable(results.getUniqueMappedResult());

    }
}
