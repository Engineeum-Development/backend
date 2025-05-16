package genum.learn.service;

import genum.course.service.CourseService;
import genum.genumUser.repository.projection.GenumUserWithIDFirstNameLastName;
import genum.genumUser.service.GenumUserService;
import genum.learn.dto.*;
import genum.learn.model.Lesson;
import genum.learn.model.Video;
import genum.learn.repository.LessonRepository;
import genum.learn.repository.VideoDeleteStatusRepository;
import genum.learn.repository.VideoRepository;
import genum.learn.repository.VideoUploadStatusRepository;
import genum.learn.repository.projection.AverageRating;
import genum.shared.Sse.service.SseEmitterService;
import genum.shared.course.DTO.CourseDTO;
import genum.shared.learn.exception.LessonNotFoundException;
import genum.shared.learn.exception.LessonWithTitleAlreadyExists;
import genum.shared.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class LearningServiceTest {

    @Mock
    private CourseService courseService;
    @Mock
    private VideoRepository videoRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private RatingService ratingService;
    @Mock
    private ReviewService reviewService;
    @Mock
    private VideoStorageService videoStorageService;
    @Mock
    private VideoUploadStatusRepository videoUploadStatusRepository;
    @Mock
    private VideoDeleteStatusRepository videoDeleteStatusRepository;
    @Mock
    private SseEmitterService sseEmitterService;
    @Mock
    private GenumUserService genumUserService;
    @InjectMocks
    private LearningService learningService;


    public static final String courseIdOne = UUID.randomUUID().toString();
    public static final String courseIdTwo = UUID.randomUUID().toString();
    public static final String currentUserId = UUID.randomUUID().toString();
    public static final String courseOneName = "course 1";
    public static final String courseTwoName = "course 2";
    private static final List<CourseDTO> listOfCourseDTOtoBeReturned = List.of(
            new CourseDTO(courseIdOne,
                    courseOneName,
                    currentUserId,
                    20000,
                    "Some test course",
                    LocalDateTime.now().toString()),
            new CourseDTO(courseIdTwo,
                    courseTwoName,
                    currentUserId,
                    20000,
                    "Some test course",
                    LocalDateTime.now().toString())
    );
    private static final List<CourseResponse> listOfCourseResponse = List.of(
            new CourseResponse(courseIdOne,courseOneName, 0L, 0),
            new CourseResponse(courseIdTwo,courseTwoName, 0L, 0)
    );
    @Test
    void shouldGetAPagedResponseForGetAllCourses() {
        given(courseService.findAllCourses(any(Pageable.class)))
                .willReturn(new PageImpl<>(listOfCourseDTOtoBeReturned));
        given(ratingService.getRatingForCourse(anyString()))
                .willReturn(new AverageRating(0));

        var responsePageResponse = learningService.getAllCourses(Pageable.ofSize(20));

        assertThat(responsePageResponse.content()).hasSize(listOfCourseDTOtoBeReturned.size());
        assertThat(responsePageResponse.content()).contains(listOfCourseResponse.toArray(CourseResponse[]::new));

        then(courseService).should(times(1)).findAllCourses(any(Pageable.class));
    }
    @Test
    void shouldGetAllMyCourseForCurrentUser() {
        given(securityUtils.getCurrentAuthenticatedUserId()).willReturn(currentUserId);
        given(courseService.findCourseWithUploaderId(anyString(),any(Pageable.class)))
                .willReturn(new PageImpl<>(listOfCourseDTOtoBeReturned));
        given(ratingService.getRatingForCourse(anyString())).willReturn(new AverageRating(0));
        var responsePageResponse = learningService
                .getAllMyCourses(Pageable.ofSize(20));
        assertThat(responsePageResponse.content()).hasSize(listOfCourseDTOtoBeReturned.size());
        assertThat(responsePageResponse.content()).contains(listOfCourseResponse.toArray(CourseResponse[]::new));

        then(courseService).should(times(1))
                .findCourseWithUploaderId(anyString(),any(Pageable.class));
    }

    public static final String lessonOneId = UUID.randomUUID().toString();
    public static final String lessonOneTitle = "Lesson One Title";
    public static final String lessonOneDesc = "Lesson One Description";
    public static final String lessonOneCont = "Lesson One Content";
    public static final String lessonTwoId = UUID.randomUUID().toString();
    public static final String lessonTwoTitle = "Lesson Two Title";
    public static final String lessonTwoDesc = "Lesson Two Description";
    public static final String lessonTwoCont = "Lesson Two Content";
    private static final List<LessonDTO> lessonsForCourseOne = List.of(
            new LessonDTO(lessonOneId,courseIdOne, lessonOneTitle, lessonOneDesc,lessonOneCont,0),
            new LessonDTO(lessonTwoId,courseIdOne, lessonTwoTitle, lessonTwoDesc,lessonTwoCont,0)
    );

    @Test
    void shouldGetAllLessonsForTheFollowingCourse() {
        given(lessonRepository.findAllByCourseId(anyString(),any(Pageable.class))).willReturn(new PageImpl<>(lessonsForCourseOne));

        var responsePageResponse = learningService.getAllLessonsForCourse(courseIdOne, Pageable.ofSize(10));
        assertThat(responsePageResponse.content()).hasSize(2);

        then(lessonRepository).should(times(1)).findAllByCourseId(anyString(),any(Pageable.class));
    }


    public static final Lesson lessonReturned = new Lesson(
            "full lesson",
            "Full lesson description",
            "Full lesson content",
            courseIdOne
    );
    static {
        lessonReturned.setReferenceId(UUID.randomUUID().toString());
    }
    public static final Video videoReturnedWithVideoUrl = new Video("Video description", "Video Title", lessonReturned.getReferenceId());
    static {
        videoReturnedWithVideoUrl.setUploadVideoFileUrl("https://example.org");
    }
    @Test
    void givenLessonExistsAndVideoExistsShouldReturnFullLessonResponse() {
        given(lessonRepository.findDTOByReferenceId(anyString())).willReturn(Optional.of(lessonReturned.toLessonDTO()));
        given(videoRepository.findByLessonId(anyString())).willReturn(Optional.of(videoReturnedWithVideoUrl));

        var lessonFullResponse = learningService.getFullLessonResponseByLessonId(lessonReturned.getReferenceId());

        assertThat(lessonFullResponse.content()).isEqualTo(lessonReturned.getContent());
        assertThat(lessonFullResponse.description()).isEqualTo(lessonReturned.getDescription());
        assertThat(lessonFullResponse.title()).isEqualTo(lessonReturned.getTitle());

        then(lessonRepository).should(times(1)).findDTOByReferenceId(anyString());
        then(videoRepository).should(times(1)).findByLessonId(anyString());

    }

    @Test
    void givenLessonNotFoundShouldThrowLessonNotFoundException() {
        assertThatExceptionOfType(LessonNotFoundException.class)
                .isThrownBy(() -> learningService.getFullLessonResponseByLessonId(anyString()));
        then(videoRepository).should(never()).findByLessonId(anyString());
    }

    public static final CourseDTO createdCourseDTO = new CourseDTO(courseIdOne,
            courseOneName,
            currentUserId,
            20000,
            "Some test course",
            LocalDateTime.now().toString());

    public static final CourseUploadRequest courseRequest = new CourseUploadRequest(courseOneName, lessonOneDesc, 20000);

    @Test
    void givenValidCourseCreationRequest() {
        given(securityUtils.getCurrentAuthenticatedUserId()).willReturn(currentUserId);
        given(courseService.createCourse(any(CourseDTO.class))).willReturn(createdCourseDTO);

        var result = learningService.uploadCourse(courseRequest);
        assertThat(result.courseId()).isEqualTo(createdCourseDTO.referenceId());
        assertThat(result.name()).isEqualTo(createdCourseDTO.name());

        then(courseService).should(times(1)).createCourse(any(CourseDTO.class));
        then(securityUtils).should(times(1)).getCurrentAuthenticatedUserId();
    }

    public static final LessonUploadRequest createLesson = new LessonUploadRequest(courseIdOne,
            lessonReturned.getTitle(),
            lessonReturned.getDescription(),
            lessonReturned.getContent());

    @Test
    void shouldUploadLessonGivenValidRequest() {
        given(lessonRepository.existsByTitle(anyString())).willReturn(false);
        given(lessonRepository.save(any(Lesson.class))).willReturn(lessonReturned);

        var result = learningService.uploadLesson(createLesson);
        assertThat(result.description()).isEqualTo(createLesson.description());
        assertThat(result.title()).isEqualTo(createLesson.title());

        then(lessonRepository).should(times(1)).save(any(Lesson.class));
    }

    @Test
    void shouldThrowLessonWithTitleAlreadyExists() {
        given(lessonRepository.existsByTitle(anyString())).willReturn(true);
        assertThatExceptionOfType(LessonWithTitleAlreadyExists.class)
                .isThrownBy(() -> learningService.uploadLesson(createLesson));

        then(lessonRepository).should(times(1)).existsByTitle(anyString());
        then(lessonRepository).should(never()).save(any(Lesson.class));
    }


    public static final LessonUpdateRequest lessonUpdateRequest = new LessonUpdateRequest("Lesson Updated Title",
            "Lesson updated description",
            "Lesson updated content"
    );

    public static final Lesson updatedLesson = new Lesson(lessonUpdateRequest.title(),
            lessonUpdateRequest.description(),
            lessonUpdateRequest.content(),
            courseIdOne);

    @Test
    void shouldUpdateLesson() {
        given(lessonRepository.findByReferenceId(anyString())).willReturn(Optional.of(lessonReturned));
        given(lessonRepository.save(any(Lesson.class))).willReturn(updatedLesson);

        var result = learningService.updateLesson(lessonOneId, lessonUpdateRequest);

        assertThat(result.title()).isEqualTo(updatedLesson.getTitle());
        assertThat(result.description()).isEqualTo(updatedLesson.getDescription());

        then(lessonRepository).should(times(1)).save(any(Lesson.class));
        then(lessonRepository).should(times(1)).findByReferenceId(anyString());
    }

    public static final List<ReviewDTO> reviewsDTO = List.of(
            new ReviewDTO(currentUserId, courseIdOne, 3, "good enough"),
            new ReviewDTO(UUID.randomUUID().toString(), courseIdOne, 5, "Exceptional"),
            new ReviewDTO(UUID.randomUUID().toString(), courseIdOne, 4, "Exceptional"),
            new ReviewDTO(UUID.randomUUID().toString(), courseIdOne, 1, "Exceptional"),
            new ReviewDTO(UUID.randomUUID().toString(), courseIdOne, 2, "Exceptional"),
            new ReviewDTO(UUID.randomUUID().toString(), courseIdOne, 3, "Exceptional")
    );

    @Test
    void shouldReturnCourseByIdIfExists() {
        given(courseService.findCourseByReference(anyString())).willReturn(createdCourseDTO);
        given(reviewService.findAllReviewsByCourseId(anyString(),any(Pageable.class))).willReturn(reviewsDTO);
        given(genumUserService.getUserFirstNameAndLastNameWithId(anyString()))
                .willReturn(new GenumUserWithIDFirstNameLastName(currentUserId,"Divine", "Maduka"));
        given(ratingService.getRatingForCourse(anyString())).willReturn(new AverageRating(2.0));
        var reviewData = learningService.getCourse(courseIdOne);
        assertThat(reviewData.reviews()).hasSize(6);

        then(courseService).should(times(1)).findCourseByReference(anyString());
        then(reviewService).should(times(1)).findAllReviewsByCourseId(anyString(),any(Pageable.class));
        then(genumUserService).should(times(reviewData.reviews().size())).getUserFirstNameAndLastNameWithId(anyString());

    }

    public static final MultipartFile mockMultipartFile = new MockMultipartFile("test","test.mp4",
            MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE, new byte[]{});

    public static final VideoUploadRequest videoUploadRequest =
            new VideoUploadRequest(UUID.randomUUID().toString(),
                    lessonOneId,
                    "This is a test video",
                    "This is the video title",
                    new HashSet<>() );
    public static final Video videoReturnedAfterSave = new Video(
            videoUploadRequest.description(),
            videoUploadRequest.title()
            ,lessonOneId
    );
    @Test
    void shouldStartUploadProcess() {
        given(videoRepository.save(any(Video.class))).willReturn(videoReturnedAfterSave);
        given(lessonRepository.existsByReferenceId(anyString())).willReturn(true);
        var result = learningService.addVideoToLesson(videoUploadRequest,mockMultipartFile);
        log.info("result uploadId {} | request uploadId {}",result.uploadId(), videoUploadRequest.uploadId());
        log.info("result videoId {} | saved videoId {}",result.videoId(), videoReturnedAfterSave.getVideoId());
        assertThat(result.videoId()).isEqualTo(videoReturnedAfterSave.getVideoId());
        assertThat(result.uploadId()).isEqualTo(videoUploadRequest.uploadId());
        then(videoRepository).should(times(2)).save(any(Video.class));
        then(sseEmitterService).should(times(4)).sendProgress(anyString(),anyInt());

    }

    @Test
    void shouldThrowLessonNotFoundExceptionIfLessonNotExists() {
        given(lessonRepository.existsByReferenceId(anyString())).willReturn(false);

        assertThatExceptionOfType(LessonNotFoundException.class)
                .isThrownBy(() -> learningService.addVideoToLesson(videoUploadRequest,mockMultipartFile));
        then(videoRepository).should(never()).save(any(Video.class));
        then(sseEmitterService).should(never()).sendProgress(anyString(),anyInt());

    }





























}
