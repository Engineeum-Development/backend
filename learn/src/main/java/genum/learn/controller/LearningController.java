package genum.learn.controller;

import genum.learn.dto.*;
import genum.learn.service.LearningService;
import genum.shared.DTO.response.ResponseDetails;
import genum.shared.exception.UploadSizeLimitExceededException;
import genum.shared.learn.exception.LessonNotFoundException;
import genum.shared.learn.exception.VideoNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/learn")
public class LearningController {

    private final LearningService learningService;
    @Value("${video.upload.max-file-size}")
    private String maxUploadSize;

    @GetMapping("/course")
    public ResponseEntity<ResponseDetails<Page<CourseResponse>>> getAllCourses(@PageableDefault Pageable pageable) {
        var responseDetails = new ResponseDetails<>("Successful",
                HttpStatus.OK.toString(),
                learningService.getAllCourses(pageable));
        return ResponseEntity.ok(responseDetails);
    }

    @GetMapping("/course/my-course")
    public ResponseEntity<ResponseDetails<Page<CourseResponse>>> getAllMyCourses(@PageableDefault Pageable pageable) {
        var responseDetails = new ResponseDetails<>("Successful",
                HttpStatus.OK.toString(),
                learningService.getAllMyCourses(pageable));
        return ResponseEntity.ok(responseDetails);
    }

    @GetMapping("/course/{id}")
    public ResponseEntity<ResponseDetails<CourseResponseFull>> getCourse(@PathVariable("id") String courseId) {
        var responseDetails = new ResponseDetails<>("Successful",
                HttpStatus.OK.toString(),
                learningService.getCourse(courseId));
        return ResponseEntity.ok(responseDetails);
    }

    @GetMapping("/auth/course/{id}")
    public ResponseEntity<ResponseDetails<Boolean>> checkIfUserIsEnrolledToCourse(@PathVariable("id") String courseId) {
        var responseDetails = new ResponseDetails<>("Successful",
                HttpStatus.OK.toString(),
                learningService.getIfCurrentUserIsAuthorizedToAccessCourse(courseId));
        return ResponseEntity.ok(responseDetails);
    }

    @PostMapping("/course")
    public ResponseEntity<ResponseDetails<CourseResponse>> uploadCourse(@Valid @RequestBody CreateCourseRequest createCourseRequest,
                                                                        HttpServletRequest httpServletRequest) {
        CourseResponse courseResponse = learningService.uploadCourse(createCourseRequest);
        var responseDetails = new ResponseDetails<>("Successful",
                HttpStatus.CREATED.toString(),
                courseResponse);
        return ResponseEntity.created(URI.create(httpServletRequest.getRequestURI())).body(responseDetails);
    }

    @GetMapping("/lesson")
    public ResponseEntity<ResponseDetails<Page<LessonResponse>>> getLessonsInCourse(@PageableDefault Pageable pageable,
                                                                                    @RequestParam(value = "course_id") String courseId) {
        Page<LessonResponse> lessonResponses = learningService.getAllLessonsForCourse(courseId, pageable);
        ResponseDetails<Page<LessonResponse>> responseDetails = new ResponseDetails<>("Successful",
                HttpStatus.OK.toString(), lessonResponses);
        return ResponseEntity.ok(responseDetails);

    }

    @GetMapping("/lesson/{id}")
    public ResponseEntity<ResponseDetails<LessonResponseFull>> getFullLesson(@PathVariable("id") String lessonId) {
        LessonResponseFull lessonResponse = learningService.getFullLessonResponseByLessonId(lessonId);
        var responseDetails = new ResponseDetails<>("Successful", HttpStatus.OK.toString(), lessonResponse);
        return ResponseEntity.ok(responseDetails);
    }

    @PostMapping("/lesson")
    public ResponseEntity<ResponseDetails<LessonResponse>> uploadLesson(
            @RequestBody @Valid CreateLessonRequest createLessonRequest,
            HttpServletRequest httpServletRequest) {
        var responseDetails = new ResponseDetails<>("Successful",
                HttpStatus.CREATED.toString(),
                learningService.uploadLesson(createLessonRequest));
        return ResponseEntity.created(URI.create(httpServletRequest.getRequestURI())).body(responseDetails);
    }

    @PutMapping("/lesson/{id}")
    public ResponseEntity<ResponseDetails<LessonResponse>> updateLesson(
            @PathVariable("id") String id,
            @RequestBody LessonUpdateRequest lessonUpdateRequest) {
        var responseDetails = new ResponseDetails<>("Successful",
                HttpStatus.OK.toString(),
                learningService.updateLesson(id, lessonUpdateRequest));
        return ResponseEntity.ok(responseDetails);

    }

    @PostMapping("/lesson/video")
    public ResponseEntity<ResponseDetails<NonChunkedVideoUploadResponse>> uploadVideoForLesson(
            @RequestPart("metadata") VideoUploadRequest uploadRequest,
            @RequestPart("video") MultipartFile file,
            HttpServletRequest httpServletRequest) {

        var MAX_FILE_SIZE = DataSize.parse(maxUploadSize);
        var file_data_size = DataSize.ofBytes(file.getSize());

        if (file_data_size.toBytes() > MAX_FILE_SIZE.toBytes()) {
            throw new UploadSizeLimitExceededException(file_data_size.toBytes(), MAX_FILE_SIZE.toBytes());
        }
        var responseDetails = new ResponseDetails<>("Successful",
                HttpStatus.CREATED.toString(),
                learningService.addVideoToLesson(uploadRequest, file));
        return ResponseEntity
                .created(URI.create(httpServletRequest.getRequestURI()))
                .body(responseDetails);

    }

    @DeleteMapping("/lesson/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable("id") String lessonId) {
        try {
            learningService.deleteLesson(lessonId);
            return ResponseEntity.ok().build();
        } catch (LessonNotFoundException e) {
            return ResponseEntity.noContent().build();
        }
    }

    @DeleteMapping("/lesson/video/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable("id") String videoId) {
        try {
            learningService.deleteVideo(videoId);
            return ResponseEntity.ok().build();
        } catch (VideoNotFoundException e) {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/lesson/review/")
    public ResponseEntity<ReviewData> reviewLesson(@RequestBody ReviewRequest reviewRequest) {
        var reviewData = learningService.reviewLesson(reviewRequest);
        return ResponseEntity
                .ok(reviewData);
    }
}
