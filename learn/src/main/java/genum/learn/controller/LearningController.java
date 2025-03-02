package genum.learn.controller;

import genum.learn.dto.*;
import genum.learn.service.LearningService;
import genum.shared.DTO.response.ResponseDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/learn")
public class LearningController {

    private final LearningService learningService;

    @GetMapping("/course")
    public ResponseEntity<ResponseDetails<Page<CourseResponse>>> getAllCourses(@PageableDefault Pageable pageable) {
        var responseDetails = new ResponseDetails<>("Successful",
                HttpStatus.OK.toString(),
                learningService.getAllCourses(pageable));
        return ResponseEntity.ok(responseDetails);
    }

    @GetMapping("/course/{id}")
    public ResponseEntity<ResponseDetails<CourseDetailedResponse>> getCourse(@PathVariable("id") String courseId) {
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

    @PostMapping("/course/upload")
    public ResponseEntity<ResponseDetails<CourseResponse>> uploadCourse(@Valid @RequestBody CreateCourseRequest createCourseRequest, HttpServletRequest httpServletRequest) {
        var courseResponse = learningService.uploadCourse(createCourseRequest);
        var responseDetails = new ResponseDetails<>("Successful",
                HttpStatus.CREATED.toString(),
                courseResponse);
        return ResponseEntity.created(URI.create(httpServletRequest.getRequestURI())).body(responseDetails);
    }
    @PostMapping("/lesson/upload")
    public ResponseEntity<ResponseDetails<LessonResponse>> uploadLesson(@RequestBody @Valid CreateLessonRequest createLessonRequest, HttpServletRequest httpServletRequest) {
        var responseDetails = new ResponseDetails<>("Successful",
                HttpStatus.CREATED.toString(),
                learningService.uploadLesson(createLessonRequest));
        return ResponseEntity.created(URI.create(httpServletRequest.getRequestURI())).body(responseDetails);
    }
    @GetMapping("/lesson/video/upload/{id}")
    public ResponseEntity<ResponseDetails<VideoUploadResponse>> getUploadStatus(@PathVariable("id") String videoId) {
        var responseDetails = new ResponseDetails<>("Successful",
                HttpStatus.OK.toString(),
                learningService.getUploadStatus(videoId));
        return ResponseEntity.ok(responseDetails);
    }
    @PostMapping("/lesson/video/upload")
    public ResponseEntity<ResponseDetails<VideoUploadResponse>> uploadVideoForLesson(@RequestPart("metadata")VideoUploadRequest uploadRequest, @RequestPart("video")MultipartFile file, HttpServletRequest httpServletRequest) {
        var responseDetails = new ResponseDetails<>("Successful",
                HttpStatus.CREATED.toString(),
                learningService.addVideoToLesson(uploadRequest, file));

        return ResponseEntity.created(URI.create(httpServletRequest.getRequestURI())).body(responseDetails);
    }
}
