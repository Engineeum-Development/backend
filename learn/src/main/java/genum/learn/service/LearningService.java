package genum.learn.service;

import genum.learn.dto.*;
import genum.learn.model.Lesson;
import genum.learn.repository.LessonRepository;
import genum.learn.repository.ReviewRepository;
import genum.learn.repository.VideoRepository;
import genum.learn.repository.VideoSeriesRepository;
import genum.product.service.ProductService;
import genum.shared.product.DTO.CourseDTO;
import genum.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningService {

    private final ProductService productService;
    private final VideoRepository videoRepository;
    private final VideoSeriesRepository videoSeriesRepository;
    private final LessonRepository lessonRepository;
    private final SecurityUtils securityUtils;
    private final RatingService ratingService;
    private final ReviewRepository reviewRepository;

    public Page<CourseResponse> getAllCourses(Pageable pageable) {
        var courses = productService.findAllCourses(pageable);
        return courses.map(courseDTO -> new CourseResponse(courseDTO.referenceId(), courseDTO.name(), courseDTO.numberOfEnrolledUsers(), ratingService.generateRatingForCourse(courseDTO.referenceId())));
    }

    public CourseResponse uploadCourse(CreateCourseRequest createCourseRequest) {
        var userId = securityUtils.getCurrentAuthenticatedUserId();

        var courseDTO = new CourseDTO(null,
                createCourseRequest.name(),
                userId,
                createCourseRequest.price(),
                createCourseRequest.description(),
                LocalDateTime.now().toString());
        courseDTO = productService.createCourse(courseDTO);
        return new CourseResponse(courseDTO.referenceId(), courseDTO.name(), courseDTO.numberOfEnrolledUsers(), ratingService.generateRatingForCourse(courseDTO.referenceId()));
    }
    public LessonResponse uploadLesson(CreateLessonRequest createLessonRequest) {
        var lesson = new Lesson(createLessonRequest.title(), createLessonRequest.description(), createLessonRequest.content(), createLessonRequest.courseId());
        lesson = lessonRepository.save(lesson);
        return new LessonResponse(lesson.getReferenceId(), lesson.getTitle(), lesson.getDescription());
    }


    public CourseDetailedResponse getCourse(String courseID) {
        var course = productService.findCourseByReference(courseID);
        var reviewData = reviewRepository.findAllByCourseId(courseID).stream()
                .limit(20)
                .map(review -> new ReviewData(review.getComment(), String.valueOf(review.getRating())))
                .collect(Collectors.toSet());
        return new CourseDetailedResponse(
                course.name(),
                course.description(),
                course.uploader(),
                String.valueOf(course.numberOfEnrolledUsers()),
                String.valueOf(ratingService.generateRatingForCourse(courseID)),
                course.uploadDate(),
                reviewData
        );

    }

    public boolean getIfCurrentUserIsAuthorizedToAccessCourse(String courseID) {
        String userId = securityUtils.getCurrentAuthenticatedUserId();
        return productService.userIdHasEnrolledForCourse(courseID, userId);

    }
}
