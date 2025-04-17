package genum.learn.service;

import genum.learn.dto.ReviewDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {
    private final ReviewService reviewService;


    @Cacheable(value = "course_rating", key = "#courseId")
    public int generateRatingForCourse(String courseId) {
        var reviews = reviewService.findAllReviewsByCourseId(courseId);
        if (reviews.isEmpty()){
            return 0;
        }
        var ratingSum = reviews.stream()
                .map(ReviewDTO::rating)
                .reduce(Integer::sum).orElseThrow();
        return Math.floorDiv(ratingSum, reviews.size());

    }

    @CacheEvict(value = "course_rating", key = "#courseId")
    public void updateRating(String courseId) {
        log.info("Rating cache for {} evicted", courseId);
    }
}
