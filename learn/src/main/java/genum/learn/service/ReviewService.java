package genum.learn.service;

import genum.genumUser.repository.projection.GenumUserWithIDFirstNameLastName;
import genum.genumUser.service.GenumUserService;
import genum.learn.dto.ReviewDTO;
import genum.learn.dto.ReviewData;
import genum.learn.model.Review;
import genum.learn.repository.ReviewRepository;
import genum.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final GenumUserService genumUserService;
    private final RatingService ratingService;

    public ReviewData addReview(ReviewDTO reviewDTO) {
        ratingService.evictRatingInCache();
        Review review = new Review(reviewDTO.comment(), reviewDTO.rating(), reviewDTO.reviewerId(), reviewDTO.courseId());
        review = reviewRepository.save(review);
        GenumUserWithIDFirstNameLastName firstNameLastName = genumUserService.getUserFirstNameAndLastNameWithId(reviewDTO.reviewerId());
        return new ReviewData(
                "%s %s".formatted(firstNameLastName.firstName(), firstNameLastName.lastName()),
                reviewDTO.comment(),
                (int) review.getRating());
    }
    public List<ReviewDTO> findAllReviewsByCourseId(String courseId, Pageable pageable) {
        var reviews = reviewRepository.findAllByCourseId(courseId, pageable);
        return reviews.stream().map(Review::toDTO).toList();
    }

}
