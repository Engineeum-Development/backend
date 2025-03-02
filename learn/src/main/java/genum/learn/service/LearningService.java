package genum.learn.service;

import genum.learn.dto.*;
import genum.learn.model.Lesson;
import genum.learn.model.Video;
import genum.learn.model.VideoSeries;
import genum.learn.model.VideoUploadStatusModel;
import genum.learn.repository.*;
import genum.product.service.ProductService;
import genum.shared.learn.exception.LessonNotFoundException;
import genum.shared.learn.exception.VideoNotFoundException;
import genum.shared.learn.exception.VideoSeriesNotFoundException;
import genum.shared.product.DTO.CourseDTO;
import genum.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
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
        return new LessonResponse(lesson.getReferenceId(), lesson.getTitle(), lesson.getDescription(), null);
    }

    @Transactional
    public VideoUploadResponse addVideoToLesson(VideoUploadRequest uploadRequest, MultipartFile file) {
        boolean requestContainsVideoSeriesIdNotLessonId = (uploadRequest.videoSeriesId() != null && uploadRequest.lessonId() == null);
        boolean requestContainsLessonIdNotVideoSeriesId = (uploadRequest.lessonId() != null && uploadRequest.videoSeriesId() == null);
        if (requestContainsLessonIdNotVideoSeriesId) {
            var lesson = lessonRepository.findByReferenceId(uploadRequest.lessonId()).orElseThrow(LessonNotFoundException::new);
            var video = new Video(uploadRequest.videoNumber(), uploadRequest.description(), uploadRequest.title());
            var videoSeries = new VideoSeries(video.getVideoId(), lesson.getTitle(), lesson.getReferenceId(), lesson.getDescription(), uploadRequest.tags());
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
        CompletableFuture.runAsync(() -> uploadVideo(file,videoId));
        return new VideoUploadResponse(video.getVideoId(),videoSeries.getReference(), videoUpload.getVideoUploadStatus());
    }

    public VideoUploadResponse getUploadStatus(String videoId) {
        var videoStatus = videoUploadStatusRepository
                .getVideoUploadStatusModelByVideoId(videoId)
                .orElse(new VideoUploadStatusModel(videoId,VideoUploadStatus.PENDING));
        return new VideoUploadResponse(videoId,null, videoStatus.getVideoUploadStatus());
    }
    @Async("videoUploadExecutor")
    public void uploadVideo(MultipartFile multipartFile, String videoId) {
        var video = videoRepository.findByVideoId(videoId).orElseThrow(VideoNotFoundException::new);
        var videoUpload = videoUploadStatusRepository
                .getVideoUploadStatusModelByVideoId(videoId)
                .orElse(new VideoUploadStatusModel(videoId, VideoUploadStatus.PENDING));
        try {
            var uploadUrl = videoService.uploadVideo(multipartFile);
            video.setUploadVideoFileUrl(uploadUrl);
            videoRepository.save(video);
            videoUpload.setVideoUploadStatus(VideoUploadStatus.SUCCESS);

        } catch (IOException e) {
            videoUpload.setVideoUploadStatus(VideoUploadStatus.FAILED);
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
        videoRepository.deleteByVideoId(videoId);
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
