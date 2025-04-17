package genum.learn.service;

import genum.learn.dto.ReviewDTO;
import genum.learn.dto.ReviewData;
import genum.learn.model.Review;
import genum.learn.repository.ReviewRepository;
import genum.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RatingService ratingService;
    private final SecurityUtils securityUtils;

    public ReviewData addReview(ReviewDTO reviewDTO) {
        Review review = new Review(reviewDTO.comment(), reviewDTO.rating(), securityUtils.getCurrentAuthenticatedUserId(), reviewDTO.courseId());
        review = reviewRepository.save(review);
        ratingService.updateRating(reviewDTO.courseId());
        return new ReviewData(reviewDTO.comment(), String.valueOf(review.getRating()));
    }
    public List<ReviewDTO> findAllReviewsByCourseId(String courseId) {
        var reviews = reviewRepository.findAllByCourseId(courseId);
        return reviews.stream().map(Review::toDTO).toList();
    }

}
