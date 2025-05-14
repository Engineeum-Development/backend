package genum.learn.repository;

import genum.learn.repository.projection.AverageRating;

import java.util.Optional;

public interface ReviewRepositoryCustom {

    Optional<AverageRating> findRatingFromReviewsByCourseId(String courseId);
}
