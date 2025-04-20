package genum.learn.service;

import genum.learn.dto.*;
import genum.learn.enums.VideoDeleteStatus;
import genum.learn.enums.VideoUploadStatus;
import genum.learn.model.Lesson;
import genum.learn.model.Video;
import genum.learn.model.VideoDeleteStatusModel;
import genum.learn.model.VideoUploadStatusModel;
import genum.learn.repository.LessonRepository;
import genum.learn.repository.VideoDeleteStatusRepository;
import genum.learn.repository.VideoRepository;
import genum.learn.repository.VideoUploadStatusRepository;
import genum.product.service.ProductService;
import genum.shared.learn.exception.LessonNotFoundException;
import genum.shared.learn.exception.VideoNotFoundException;
import genum.shared.product.DTO.CourseDTO;
import genum.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningService {

    private final ProductService productService;
    private final VideoRepository videoRepository;
    private final LessonRepository lessonRepository;
    private final SecurityUtils securityUtils;
    private final RatingService ratingService;
    private final ReviewService reviewService;
    private final VideoService videoService;
    private final VideoUploadStatusRepository videoUploadStatusRepository;
    private final VideoDeleteStatusRepository videoDeleteStatusRepository;

    public Page<CourseResponse> getAllCourses(Pageable pageable) {
        var courses = productService.findAllCourses(pageable);
        return courses.map(courseDTO -> new CourseResponse(courseDTO.referenceId(),
                courseDTO.name(),
                courseDTO.numberOfEnrolledUsers(),
                ratingService.getRatingForCourse(courseDTO.referenceId())));
    }

    public Page<CourseResponse> getAllMyCourses(Pageable pageable) {
        var currentUserId = securityUtils.getCurrentAuthenticatedUserId();
        var courses = productService.findCourseWithUploaderId(currentUserId, pageable);
        return courses.map(courseDTO -> new CourseResponse(courseDTO.referenceId(),
                courseDTO.name(),
                courseDTO.numberOfEnrolledUsers(),
                ratingService.getRatingForCourse(courseDTO.referenceId())));
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
        return new CourseResponse(courseDTO.referenceId(), courseDTO.name(), courseDTO.numberOfEnrolledUsers(), ratingService.getRatingForCourse(courseDTO.referenceId()));
    }

    public LessonResponse uploadLesson(CreateLessonRequest createLessonRequest) {
        var lesson = new Lesson(createLessonRequest.title(), createLessonRequest.description(), createLessonRequest.content(), createLessonRequest.courseId());
        lesson = lessonRepository.save(lesson);
        return new LessonResponse(lesson.getReferenceId(), lesson.getTitle(), lesson.getDescription());
    }

    @Transactional
    public VideoUploadResponse addVideoToLesson(VideoUploadRequest uploadRequest, MultipartFile file) {
            var lesson = lessonRepository.findByReferenceId(uploadRequest.lessonId()).orElseThrow(LessonNotFoundException::new);
            var video = new Video(uploadRequest.description(), uploadRequest.title(), lesson.getReferenceId());
            return getVideoUploadResponse(file,video);
    }

    private VideoUploadResponse getVideoUploadResponse(MultipartFile file,Video video) {
        var videoId = videoRepository.save(video).getVideoId();
        var videoUpload = new VideoUploadStatusModel(video.getVideoId(), VideoUploadStatus.PENDING);
        videoUploadStatusRepository.save(videoUpload);
        CompletableFuture.runAsync(() -> uploadVideo(file, videoId));
        return new VideoUploadResponse(video.getVideoId(),videoUpload.getVideoUploadStatus());
    }

    public VideoUploadResponse getUploadStatus(String videoId) {
        var videoStatus = videoUploadStatusRepository
                .getVideoUploadStatusModelByVideoId(videoId)
                .orElse(new VideoUploadStatusModel(videoId, VideoUploadStatus.PENDING));
        return new VideoUploadResponse(videoId,  videoStatus.getVideoUploadStatus());
    }


    public void uploadVideo(MultipartFile multipartFile, String videoId) {
        var video = videoRepository.findByVideoId(videoId).orElseThrow(VideoNotFoundException::new);
        var videoUpload = videoUploadStatusRepository
                .getVideoUploadStatusModelByVideoId(videoId)
                .orElse(new VideoUploadStatusModel(videoId, VideoUploadStatus.PENDING));
        try {
            log.info("Started sending video {}", videoId);
            var uploadUrl = videoService.uploadVideo(multipartFile);
            video.setUploadVideoFileUrl(uploadUrl);
            videoRepository.save(video);
            videoUpload.setVideoUploadStatus(VideoUploadStatus.SUCCESS);
            log.info("Video {} was successfully uploaded", videoId);

        } catch (IOException | VideoNotFoundException e) {
            videoUpload.setVideoUploadStatus(VideoUploadStatus.FAILED);
            log.error("Video {} upload failed", videoId);
        } finally {
            videoUploadStatusRepository.save(videoUpload);
        }
    }


    public CourseDetailedResponse getCourse(String courseID) {
        var course = productService.findCourseByReference(courseID);
        var reviewData = reviewService.findAllReviewsByCourseId(courseID).stream()
                .limit(20)
                .map(review -> new ReviewData(review.comment(), String.valueOf(review.rating())))
                .collect(Collectors.toSet());
        return new CourseDetailedResponse(
                course.name(),
                course.description(),
                course.uploader(),
                String.valueOf(course.numberOfEnrolledUsers()),
                String.valueOf(ratingService.getRatingForCourse(courseID)),
                course.uploadDate(),
                reviewData
        );

    }

    public void deleteLesson(String lessonId) {
        if (lessonRepository.existsByReferenceId(lessonId)) {
            lessonRepository.deleteByReferenceId(lessonId);
        } else {
            throw new LessonNotFoundException();
        }
    }

    public void deleteVideo(String videoId) {
        var video = videoRepository.findByVideoId(videoId).orElseThrow(VideoNotFoundException::new);
        videoRepository.delete(video);
        boolean videoIsDeleted = videoService.deleteVideo(video.getUploadVideoFileUrl());
        if (videoIsDeleted) {
            videoDeleteStatusRepository.save(new VideoDeleteStatusModel(videoId, VideoDeleteStatus.SUCCESS));
        } else {
            videoDeleteStatusRepository.save(new VideoDeleteStatusModel(videoId, VideoDeleteStatus.FAILED));
        }
    }

    public boolean getIfCurrentUserIsAuthorizedToAccessCourse(String courseID) {
        String userId = securityUtils.getCurrentAuthenticatedUserId();
        return productService.userIdHasEnrolledForCourse(courseID, userId);

    }

    public ReviewData reviewLesson(ReviewRequest reviewRequest) {
        Lesson lesson = lessonRepository
                .findByReferenceId(reviewRequest.lessonId())
                .orElseThrow(LessonNotFoundException::new);

        ReviewDTO reviewDTO = new ReviewDTO(lesson.getCourseId(), reviewRequest.rating(), reviewRequest.comment());
        return reviewService.addReview(reviewDTO);
    }
}
