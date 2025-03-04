package genum.learn.service;

import genum.learn.dto.*;
import genum.learn.enums.VideoDeleteStatus;
import genum.learn.enums.VideoUploadStatus;
import genum.learn.model.*;
import genum.learn.repository.*;
import genum.product.service.ProductService;
import genum.shared.learn.exception.LessonNotFoundException;
import genum.shared.learn.exception.VideoNotFoundException;
import genum.shared.learn.exception.VideoSeriesNotFoundException;
import genum.shared.product.DTO.CourseDTO;
import genum.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
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
    private final VideoSeriesRepository videoSeriesRepository;
    private final LessonRepository lessonRepository;
    private final SecurityUtils securityUtils;
    private final RatingService ratingService;
    private final ReviewRepository reviewRepository;
    private final VideoService videoService;
    private final VideoUploadStatusRepository videoUploadStatusRepository;
    private final VideoDeleteStatusRepository videoDeleteStatusRepository;

    public Page<CourseResponse> getAllCourses(Pageable pageable) {
        var courses = productService.findAllCourses(pageable);
        return courses.map(courseDTO -> new CourseResponse(courseDTO.referenceId(),
                courseDTO.name(),
                courseDTO.numberOfEnrolledUsers(),
                ratingService.generateRatingForCourse(courseDTO.referenceId())));
    }
    public Page<CourseResponse> getAllMyCourses(Pageable pageable) {
        var currentUserId = securityUtils.getCurrentAuthenticatedUserId();
        var courses = productService.findCourseWithUploaderId(currentUserId, pageable);
        return courses.map(courseDTO -> new CourseResponse(courseDTO.referenceId(),
                courseDTO.name(),
                courseDTO.numberOfEnrolledUsers(),
                ratingService.generateRatingForCourse(courseDTO.referenceId())));
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
        return new LessonResponse(lesson.getReferenceId(), lesson.getTitle(), lesson.getDescription(), null);
    }

    @Transactional
    public VideoUploadResponse addVideoToLesson(VideoUploadRequest uploadRequest, MultipartFile file) {
        boolean requestContainsVideoSeriesIdNotLessonId = (uploadRequest.videoSeriesId() != null && uploadRequest.lessonId() == null);
        boolean requestContainsLessonIdNotVideoSeriesId = (uploadRequest.lessonId() != null && uploadRequest.videoSeriesId() == null);
        if (requestContainsLessonIdNotVideoSeriesId) {
            var lesson = lessonRepository.findByReferenceId(uploadRequest.lessonId()).orElseThrow(LessonNotFoundException::new);
            var video = new Video(uploadRequest.videoNumber(), uploadRequest.description(), uploadRequest.title());
            var videoSeries = videoSeriesRepository
                    .findByLessonReference(lesson.getReferenceId())
                    .orElse(new VideoSeries(video.getVideoId(), lesson.getTitle(), lesson.getReferenceId(), lesson.getDescription(), uploadRequest.tags()));
            return getVideoUploadResponse(file, videoSeries, video);
        } else if (requestContainsVideoSeriesIdNotLessonId) {
            var videoSeries = videoSeriesRepository.findByReference(uploadRequest.videoSeriesId()).orElseThrow(VideoSeriesNotFoundException::new);
            var video = new Video(uploadRequest.videoNumber(), uploadRequest.description(), uploadRequest.title());
            return getVideoUploadResponse(file, videoSeries, video);
        } else {
            throw new IllegalArgumentException("Request must contain either lesson id or video series id");
        }
    }

    private VideoUploadResponse getVideoUploadResponse(MultipartFile file, VideoSeries videoSeries, Video video) {
        video.setSeriesReference(videoSeries.getReference());
        var videoId = videoRepository.save(video).getVideoId();
        videoSeriesRepository.save(videoSeries);
        var videoUpload = new VideoUploadStatusModel(video.getVideoId(), VideoUploadStatus.PENDING);
        videoUploadStatusRepository.save(videoUpload);
        CompletableFuture.runAsync(() -> uploadVideo(file, videoId));
        return new VideoUploadResponse(video.getVideoId(), videoSeries.getReference(), videoUpload.getVideoUploadStatus());
    }

    public VideoUploadResponse getUploadStatus(String videoId) {
        var videoStatus = videoUploadStatusRepository
                .getVideoUploadStatusModelByVideoId(videoId)
                .orElse(new VideoUploadStatusModel(videoId, VideoUploadStatus.PENDING));
        return new VideoUploadResponse(videoId, null, videoStatus.getVideoUploadStatus());
    }

    @Async("videoUploadExecutor")
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

        } catch (IOException e) {
            videoUpload.setVideoUploadStatus(VideoUploadStatus.FAILED);
            log.error("Video {} upload failed", videoId);
        } finally {
            videoUploadStatusRepository.save(videoUpload);
        }
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

    public void deleteLesson(String lessonId) {
        lessonRepository.deleteByReferenceId(lessonId);
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
        var videoSeries = videoSeriesRepository.findByVideosAccordingToOrderContaining(videoId).orElseThrow(VideoSeriesNotFoundException::new);
        videoSeries.removeFromVideosAccordingToOrder(videoId);
        if (videoSeries.getVideosAccordingToOrder().isEmpty()) {
            videoSeriesRepository.delete(videoSeries);
        }
    }

    public boolean getIfCurrentUserIsAuthorizedToAccessCourse(String courseID) {
        String userId = securityUtils.getCurrentAuthenticatedUserId();
        return productService.userIdHasEnrolledForCourse(courseID, userId);

    }
}
