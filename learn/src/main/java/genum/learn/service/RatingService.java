package genum.learn.service;

import genum.learn.repository.projection.AverageRating;
import genum.learn.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {
    private final ReviewRepository reviewRepository;


    @Cacheable(value = "course_rating", key = "#courseId", condition = "#courseId != null")
    public AverageRating getRatingForCourse(String courseId) {
        return reviewRepository.findRatingFromReviewsByCourseId(courseId).orElse(new AverageRating(0));
    }
}
