package genum.learn.repository;

import genum.learn.model.Review;
import genum.learn.projection.AverageRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    Page<Review> findAllByCourseId(String courseId, Pageable pageable);

    @Aggregation(pipeline = {
            "{ '$match': {'courseId': ?0}}",
            """
                { '$group': {
                        '_id': null,
                        'averageRating': {'$avg': '$rating'}
                    }
            
                }
            """
    })
    Optional<AverageRating> findRatingFromReviewsByCourseId(String courseId);
}
