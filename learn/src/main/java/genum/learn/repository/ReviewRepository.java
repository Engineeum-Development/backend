package genum.learn.repository;

import genum.learn.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    Set<Review> findAllByCourseId(String courseId);
    int findRatingFromReviewsByCourseId(String courseId);
}
