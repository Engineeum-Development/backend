package genum.learn.service;

import genum.course.service.CourseService;
import genum.genumUser.service.GenumUserService;
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
import genum.shared.DTO.response.PageResponse;
import genum.shared.Sse.service.SseEmitterService;
import genum.shared.learn.exception.LessonNotFoundException;
import genum.shared.learn.exception.LessonWithTitleAlreadyExists;
import genum.shared.learn.exception.VideoNotFoundException;
import genum.shared.course.DTO.CourseDTO;
import genum.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

import java.time.LocalDateTime;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningService {

    private final CourseService courseService;
    private final VideoRepository videoRepository;
    private final LessonRepository lessonRepository;
    private final SecurityUtils securityUtils;
    private final RatingService ratingService;
    private final ReviewService reviewService;
    private final VideoStorageService videoStorageService;
    private final VideoUploadStatusRepository videoUploadStatusRepository;
    private final VideoDeleteStatusRepository videoDeleteStatusRepository;
    private final SseEmitterService sseEmitterService;
    private final GenumUserService genumUserService;

    @Cacheable(value = "course_all_page", keyGenerator = "customKeyGenerator")
    public PageResponse<CourseResponse> getAllCourses(Pageable pageable) {
        var courses = courseService.findAllCourses(pageable);
        return PageResponse.from(courses.map(courseDTO -> new CourseResponse(courseDTO.referenceId(),
                courseDTO.name(),
                courseDTO.numberOfEnrolledUsers(),
                (int) ratingService.getRatingForCourse(courseDTO.referenceId()).averageRating())));
    }

    @Cacheable(value = "courses_by_id_page", keyGenerator = "customKeyGenerator")
    public PageResponse<CourseResponse> getAllMyCourses(Pageable pageable) {
        var currentUserId = securityUtils.getCurrentAuthenticatedUserId();
        var courses = courseService.findCourseWithUploaderId(currentUserId, pageable);
        return PageResponse.from(courses.map(courseDTO -> new CourseResponse(courseDTO.referenceId(),
                courseDTO.name(),
                courseDTO.numberOfEnrolledUsers(),
                (int) ratingService.getRatingForCourse(courseDTO.referenceId()).averageRating()
        )));
    }

    public PageResponse<LessonResponse> getAllLessonsForCourse(String courseId, Pageable pageable) {
        var lessons = lessonRepository.findAllByCourseId(courseId, pageable);
        return PageResponse.from(lessons.map(lesson -> new LessonResponse(lesson.lessonId(),
                lesson.title(),
                lesson.description(), lesson.reads())));
    }

    @Cacheable(value = "lesson_by_id", key = "#lessonId", unless = "#result == null")
    public LessonResponseFull getFullLessonResponseByLessonId(String lessonId) {
        var fullLesson = lessonRepository.findDTOByReferenceId(lessonId).orElseThrow(LessonNotFoundException::new);

        CompletableFuture.runAsync(() -> addReadId(lessonId, securityUtils.getCurrentAuthenticatedUserId()));
        try {
            var videoUrl = videoRepository.findByLessonId(lessonId)
                    .orElseThrow(VideoNotFoundException::new)
                    .getUploadVideoFileUrl();
            return new LessonResponseFull(fullLesson.lessonId(),
                    fullLesson.title(),
                    fullLesson.description(),
                    fullLesson.content(),
                    videoUrl, fullLesson.reads());
        } catch (VideoNotFoundException e) {
            return new LessonResponseFull(fullLesson.lessonId(),
                    fullLesson.title(),
                    fullLesson.description(),
                    fullLesson.content(),
                    null, fullLesson.reads());
        }
    }
    private void addReadId(String lessonId, String userId) {
        var lesson = lessonRepository.findByReferenceId(lessonId).orElseThrow(LessonNotFoundException::new);
        lesson.addToReadIds(userId);
        lessonRepository.save(lesson);
    }

    public CourseResponse uploadCourse(CourseUploadRequest courseUploadRequest) {
        var userId = securityUtils.getCurrentAuthenticatedUserId();

        var courseDTO = new CourseDTO(null,
                courseUploadRequest.name(),
                userId,
                courseUploadRequest.price(),
                courseUploadRequest.description(),
                LocalDateTime.now().toString());
        courseDTO = courseService.createCourse(courseDTO);
        return new CourseResponse(courseDTO.referenceId(),
                courseDTO.name(),
                courseDTO.numberOfEnrolledUsers(),
                0);
    }

    public LessonResponse uploadLesson(LessonUploadRequest lessonUploadRequest) {
        if (lessonRepository.existsByTitle(lessonUploadRequest.title())){
            throw new LessonWithTitleAlreadyExists(lessonUploadRequest.title());
        }
        var lesson = new Lesson(lessonUploadRequest.title(),
                lessonUploadRequest.description(), lessonUploadRequest.content(),
                lessonUploadRequest.courseId());
        lesson = lessonRepository.save(lesson);
        return new LessonResponse(lesson.getReferenceId(), lesson.getTitle(), lesson.getDescription(),lesson.getReadIds().size());
    }

    public LessonResponse updateLesson(String lessonId, LessonUpdateRequest lessonUpdateRequest) {
        Lesson lesson = lessonRepository.findByReferenceId(lessonId).orElseThrow(LessonNotFoundException::new);

        if (Objects.nonNull(lessonUpdateRequest.content())) {
            if (lessonUpdateRequest.content().isEmpty() || lessonUpdateRequest.content().isBlank()) {
                var lessonContentHasChanged = !lesson.getContent().equals(lessonUpdateRequest.content());
                if (lessonContentHasChanged) {
                    lesson.setContent(lessonUpdateRequest.content());
                }
            }
        }
        if (Objects.nonNull(lessonUpdateRequest.description())) {
            if (lessonUpdateRequest.description().isEmpty() || lessonUpdateRequest.description().isBlank()) {
                var lessonDescriptionHasChanged = !lesson.getDescription().equals(lessonUpdateRequest.description());
                if (lessonDescriptionHasChanged) {
                    lesson.setDescription(lessonUpdateRequest.description());
                }
            }
        }
        if (Objects.nonNull(lessonUpdateRequest.title())) {
            if (lessonUpdateRequest.title().isEmpty() || lessonUpdateRequest.title().isBlank()) {
                var lessonTitleHasChanged = !lesson.getTitle().equals(lessonUpdateRequest.title());
                if (lessonTitleHasChanged) {
                    lesson.setTitle(lessonUpdateRequest.title());
                }
            }
        }
        lessonRepository.save(lesson);
        return new LessonResponse(lesson.getReferenceId(),lesson.getTitle(),lesson.getDescription(),lesson.getReadIds().size());
    }
    public NonChunkedVideoUploadResponse addVideoToLesson(VideoUploadRequest uploadRequest, MultipartFile file) {
        if (!lessonRepository.existsByReferenceId(uploadRequest.lessonId())) throw new LessonNotFoundException();

        var video = new Video(uploadRequest.description(), uploadRequest.title(), uploadRequest.lessonId());
        sseEmitterService.sendProgress(uploadRequest.uploadId(),0);
        var videoSaved = videoRepository.save(video);
        CompletableFuture.runAsync(() -> uploadVideo(file, videoSaved, uploadRequest.uploadId()));
        sseEmitterService.sendProgress(uploadRequest.uploadId(), 20);
        return new NonChunkedVideoUploadResponse(videoSaved.getVideoId(), uploadRequest.uploadId());
    }

    private void uploadVideo(MultipartFile multipartFile, Video video, String uploadId) {
        var videoUpload = videoUploadStatusRepository
                .getVideoUploadStatusModelByVideoId(video.getVideoId())
                .orElse(new VideoUploadStatusModel(video.getVideoId(), VideoUploadStatus.PENDING));
        try {
            log.info("Started sending video {}", video.getVideoId());
            sseEmitterService.sendProgress(uploadId, 50);
            var uploadUrl = videoStorageService.uploadVideo(multipartFile,video);
            video.setUploadVideoFileUrl(uploadUrl);
            videoRepository.save(video);
            log.info("Video {} was successfully uploaded", video.getVideoId());
            sseEmitterService.sendProgress(uploadId, 100);
            sseEmitterService.completeEmitter(uploadId, true);

        } catch (IOException | VideoNotFoundException e) {
            videoUpload.setVideoUploadStatus(VideoUploadStatus.FAILED);
            sseEmitterService.completeEmitter(uploadId, false);
        } finally {
            videoUploadStatusRepository.save(videoUpload);
        }
    }

    public CourseResponseFull getCourse(String courseID) {
        var course = courseService.findCourseByReference(courseID);
        var reviewData = reviewService.findAllReviewsByCourseId(courseID, Pageable.ofSize(20)).stream()
                .map(review -> {
                    var firstNameLastName = genumUserService.getUserFirstNameAndLastNameWithId(review.reviewerId());
                    return new ReviewData(
                            "%s %s".formatted(firstNameLastName.firstName(), firstNameLastName.lastName()),
                            review.comment(), review.rating());
                })
                .collect(Collectors.toSet());
        return new CourseResponseFull(
                course.name(),
                course.description(),
                course.uploader(),
                String.valueOf(course.numberOfEnrolledUsers()),
                String.valueOf(ratingService.getRatingForCourse(courseID)),
                course.uploadDate(),
                reviewData
        );

    }

    public void deleteLesson(String lessonId) throws LessonNotFoundException {
        if (lessonRepository.existsByReferenceId(lessonId)) {
            lessonRepository.deleteByReferenceId(lessonId);
        } else {
            throw new LessonNotFoundException();
        }
    }

    public void deleteVideo(String videoId) {
        var video = videoRepository.findByVideoId(videoId).orElseThrow(VideoNotFoundException::new);
        boolean videoIsDeleted = videoStorageService.deleteVideo(video);
        if (videoIsDeleted) {
            videoDeleteStatusRepository.save(new VideoDeleteStatusModel(videoId, VideoDeleteStatus.SUCCESS));
            videoRepository.delete(video);
        } else {
            videoDeleteStatusRepository.save(new VideoDeleteStatusModel(videoId, VideoDeleteStatus.FAILED));
        }
    }

    public boolean getIfCurrentUserIsAuthorizedToAccessCourse(String courseID) {
        return courseService.userIdHasEnrolledForCourse(courseID);
    }

    public ReviewData reviewLesson(ReviewRequest reviewRequest) {
        LessonDTO lesson = lessonRepository
                .findDTOByReferenceId(reviewRequest.lessonId())
                .orElseThrow(LessonNotFoundException::new);

        ReviewDTO reviewDTO = new ReviewDTO(securityUtils.getCurrentAuthenticatedUserId(),
                lesson.courseId(), reviewRequest.rating(), reviewRequest.comment());
        return reviewService.addReview(reviewDTO);
    }
}
