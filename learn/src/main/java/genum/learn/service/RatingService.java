package genum.learn.service;

import genum.learn.model.Review;
import genum.learn.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final ReviewRepository reviewRepository;


    @Cacheable(value = "course_rating", key = "#courseId")
    public int generateRatingForCourse(String courseId) {
        var reviews = reviewRepository.findAllByCourseId(courseId);
        if (reviews.isEmpty()){
            return 0;
        }
        var ratingSum = reviews.stream()
                .map(Review::getRating)
                .reduce(Integer::sum).orElseThrow();
        return Math.floorDiv(ratingSum, reviews.size());

    }
}
