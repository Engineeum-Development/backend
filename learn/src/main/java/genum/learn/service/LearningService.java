package genum.learn.service;

import genum.learn.domain.ChunkedVideoUploadStatusMessage;
import genum.learn.domain.VideoChunkMetadata;
import genum.learn.domain.VideoUploadStatusMessage;
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

import java.util.UUID;
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
                (int) ratingService.getRatingForCourse(courseDTO.referenceId()).averageRating()));
    }

    public Page<CourseResponse> getAllMyCourses(Pageable pageable) {
        var currentUserId = securityUtils.getCurrentAuthenticatedUserId();
        var courses = productService.findCourseWithUploaderId(currentUserId, pageable);
        return courses.map(courseDTO -> new CourseResponse(courseDTO.referenceId(),
                courseDTO.name(),
                courseDTO.numberOfEnrolledUsers(),
                (int) ratingService.getRatingForCourse(courseDTO.referenceId()).averageRating()
        ));
    }

    public Page<LessonResponse> getAllLessonsForCourse(String courseId, Pageable pageable) {
        var lessons = lessonRepository.findAllByCourseId(courseId, pageable);
        return lessons.map(lesson -> new LessonResponse(lesson.getReferenceId(), lesson.getTitle(), lesson.getDescription()));
    }

    public LessonResponseFull getFullLessonResponseByLessonId(String lessonId) {
        var fullLesson = lessonRepository.findByReferenceId(lessonId).orElseThrow(LessonNotFoundException::new);
        try {
            var videoUrl = videoRepository.findByLessonId(lessonId)
                    .orElseThrow(VideoNotFoundException::new)
                    .getUploadVideoFileUrl();
            return new LessonResponseFull(fullLesson.getReferenceId(),
                    fullLesson.getTitle(),
                    fullLesson.getDescription(),
                    fullLesson.getContent(),
                    videoUrl);
        } catch (VideoNotFoundException e) {
            return new LessonResponseFull(fullLesson.getReferenceId(), fullLesson.getTitle(), fullLesson.getDescription(), fullLesson.getContent(), null);
        }
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
        return new CourseResponse(courseDTO.referenceId(), courseDTO.name(), courseDTO.numberOfEnrolledUsers(), 0);
    }

    public LessonResponse uploadLesson(CreateLessonRequest createLessonRequest) {
        var lesson = new Lesson(createLessonRequest.title(), createLessonRequest.description(), createLessonRequest.content(), createLessonRequest.courseId());
        lesson = lessonRepository.save(lesson);
        return new LessonResponse(lesson.getReferenceId(), lesson.getTitle(), lesson.getDescription());
    }

    @Transactional
    public NonChunkedVideoUploadResponse addVideoToLesson(VideoUploadRequest uploadRequest, MultipartFile file) {
        if (!lessonRepository.existsByReferenceId(uploadRequest.lessonId())) throw new LessonNotFoundException();
        var video = new Video(uploadRequest.description(), uploadRequest.title(), uploadRequest.lessonId());
        return getVideoUploadResponse(file, video);
    }

    private NonChunkedVideoUploadResponse getVideoUploadResponse(MultipartFile file, Video video) {
        var videoId = videoRepository.save(video).getVideoId();
        final String uploadId = UUID.randomUUID().toString();
        CompletableFuture.runAsync(() -> uploadVideo(file, videoId, uploadId));
        return new NonChunkedVideoUploadResponse(video.getVideoId(), uploadId);
    }


    public void uploadVideo(MultipartFile multipartFile, String videoId, String uploadId) {
        var video = videoRepository.findByVideoId(videoId).orElseThrow(VideoNotFoundException::new);
        var videoUpload = videoUploadStatusRepository
                .getVideoUploadStatusModelByVideoId(videoId)
                .orElse(new VideoUploadStatusModel(videoId, VideoUploadStatus.PENDING));
        try {
            log.info("Started sending video {}", videoId);
            var uploadUrl = videoService.uploadVideo(multipartFile);
            video.setUploadVideoFileUrl(uploadUrl);
            videoRepository.save(video);
            log.info("Video {} was successfully uploaded", videoId);

        } catch (IOException | VideoNotFoundException e) {
            videoUpload.setVideoUploadStatus(VideoUploadStatus.FAILED);
        } finally {
            videoUploadStatusRepository.save(videoUpload);
        }
    }

    public CourseDetailedResponse getCourse(String courseID) {
        var course = productService.findCourseByReference(courseID);
        var reviewData = reviewService.findAllReviewsByCourseId(courseID, Pageable.ofSize(20)).stream()
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
