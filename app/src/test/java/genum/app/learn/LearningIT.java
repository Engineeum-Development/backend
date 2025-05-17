package genum.app.learn;

import genum.app.shared.BaseDatabaseIntegration;
import genum.course.model.Course;
import genum.genumUser.model.GenumUser;
import genum.learn.dto.*;
import genum.learn.model.Lesson;
import genum.shared.DTO.response.PageResponse;
import genum.shared.DTO.response.ResponseDetails;
import genum.shared.constant.Gender;
import genum.shared.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureJsonTesters
@Slf4j
public class LearningIT extends BaseDatabaseIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    private static CustomUserDetails userDetails;
    private static GenumUser genumUser;

    @BeforeAll
    static void setUpStatic (){
        userDetails = new CustomUserDetails("some password", "some.gmail.com");
        userDetails.setUserReferenceId(UUID.randomUUID().toString());
        genumUser = GenumUser.builder()
                .gender(Gender.MALE)
                .createdDate(LocalDateTime.now())
                .isVerified(true)
                .lastName("Main")
                .firstName("Beast")
                .customUserDetails(userDetails)
                .country("Nigeria")
                .build();
    }
    @BeforeEach
    void setupSecurity() {

        mongoTemplate.save(genumUser, "users");
        SecurityContextHolder.setContext(
                new SecurityContextImpl(UsernamePasswordAuthenticationToken.authenticated(
                        userDetails,
                        "password"
                        , List.of(new SimpleGrantedAuthority("ROLE_USER")
                        ))
                )
        );
    }
    /* ============================================ Test for getting all courses ==================================== */
    @Test
    void shouldGetAllCourses() throws Exception {
        mockMvc.perform(get("/api/learn/course")).andExpect(status().isOk());
    }

    /* ======================================= Test for getting all user courses ==================================== */

    @Test
    void shouldGetAllUsersUploadedCoursesIfAuthorized() throws Exception {
        var existingCourse = new Course("Course Existing",
                userDetails.getUserReferenceId(),
                "Course Description",
                23000);
        existingCourse.setUploaderId(userDetails.getUserReferenceId());

        existingCourse = mongoTemplate.save(existingCourse, "course");

        mockMvc.perform(
                get("/api/learn/course/my-course")
                        .with(user(userDetails))
        ).andExpect(status().isOk());

        var deleteResult = mongoTemplate.remove(existingCourse, "course");
        assertThat(deleteResult.wasAcknowledged()).isTrue();
    }
    @Test
    void shouldReturnUnauthorizedIfNotAuthorized() throws Exception {
        mockMvc.perform(
                get("/api/learn/course/my-course")
        ).andExpect(status().isUnauthorized());
    }

    /* ============================================= Test for getting a specific course ============================= */

    @Test
    void givenExistingCourse_ShouldReturnCourse() throws Exception {
        var existingCourse = new Course("Course Existing",
                userDetails.getUserReferenceId(),
                "Course Description",
                23000);

        existingCourse = mongoTemplate.save(existingCourse, "course");

        mockMvc.perform(
                    get("/api/learn/course/%s".formatted(existingCourse.getReferenceId()))
                )
                .andExpect(status().isOk());
        var deleteResult = mongoTemplate.remove(existingCourse, "course");
        assertThat(deleteResult.wasAcknowledged()).isTrue();
    }
    @Test
    void givenNonExistentCourse_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(
                    get("/api/learn/course/%s".formatted(UUID.randomUUID().toString()))
                )
                .andExpect(status().isNotFound());
    }

    /* ====================================== Test for checking if user is enrolled to course ======================= */
    @Autowired
    private JacksonTester<ResponseDetails<Boolean>> jacksonTesterForEnrolled;
    @Test
    void givenExistingCourseAndUserIsEnrolled_ShouldReturnTrue() throws Exception {
        var existingCourse = new Course("Course Existing",
                userDetails.getUserReferenceId(),
                "Course Description",
                23000);
        existingCourse.addEnrolledUsers(userDetails.getUserReferenceId());

        existingCourse = mongoTemplate.save(existingCourse, "course");

        var result = mockMvc.perform(
                get("/api/learn/auth/course/%s".formatted(existingCourse.getReferenceId()))
                        .with(user(userDetails))
        ).andExpect(status().isOk()).andReturn().getResponse();

        boolean isAuthorized = jacksonTesterForEnrolled.parseObject(result.getContentAsString(StandardCharsets.UTF_8)).getData();

        assertThat(isAuthorized).isTrue();
        var deleteResult = mongoTemplate.remove(existingCourse, "course");
        assertThat(deleteResult.wasAcknowledged()).isTrue();
    }
    @Test
    void givenExistingCourseAndUserNotEnrolled_ShouldReturnFalse() throws Exception {
        var existingCourse = new Course("Course Existing",
                userDetails.getUserReferenceId(),
                "Course Description",
                23000);
        existingCourse.addEnrolledUsers(UUID.randomUUID().toString());

        existingCourse = mongoTemplate.save(existingCourse, "course");

        var result = mockMvc.perform(
                get("/api/learn/auth/course/%s".formatted(existingCourse.getReferenceId()))
                        .with(user(userDetails))
        ).andExpect(status().isOk()).andReturn().getResponse();

        boolean isAuthorized = jacksonTesterForEnrolled.parseObject(result.getContentAsString(StandardCharsets.UTF_8)).getData();
        assertThat(isAuthorized).isFalse();

        var deleteResult = mongoTemplate.remove(existingCourse, "course");
        assertThat(deleteResult.wasAcknowledged()).isTrue();
    }

    /* ===================================== Tests for creating course ============================================== */
    @Autowired
    private JacksonTester<CourseUploadRequest> jsonUploadRequest;
    @Autowired
    private JacksonTester<ResponseDetails<CourseResponse>> jsonCourseUploadResponse;

    @Test
    void givenValidUploadCourseRequest_ShouldUploadCourseAndReturnA201Created() throws Exception {
        var validUploadCourseRequest = new CourseUploadRequest("Course Name",
                "Course Description",
                25000);

        var result = mockMvc.perform(
                post("/api/learn/course")
                        .with(user(userDetails))
                        .content(jsonUploadRequest.write(validUploadCourseRequest).getJson())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated()).andReturn().getResponse();
        CourseResponse courseUploadResponseData = jsonCourseUploadResponse
                .parseObject(result.getContentAsString(StandardCharsets.UTF_8)).getData();
        assertThat(courseUploadResponseData.name()).isEqualTo(validUploadCourseRequest.name());
    }
    @Test
    void givenInvalidUploadCourseRequest_ShouldUploadCourseAndReturnA201Created() throws Exception {
        var inValidUploadCourseRequest = new CourseUploadRequest("Course Name",
                "",
                25000);

        mockMvc.perform(
                post("/api/learn/course")
                        .with(user(userDetails))
                        .content(jsonUploadRequest.write(inValidUploadCourseRequest).getJson())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    /* ===================================== Test for getting lessons pertaining to a course ======================== */
    @Autowired
    private JacksonTester<ResponseDetails<PageResponse<LessonResponse>>> jsonPageResponseForLessons;

    @Test
    void shouldGetLessonsPertainingToAParticularCourse() throws Exception {
        var existingCourse = new Course("Course Existing",
                userDetails.getUserReferenceId(),
                "Course Description",
                23000);
        existingCourse.addEnrolledUsers(UUID.randomUUID().toString());
        var existingLesson = new Lesson("Lesson For course",
                "Description for Lesson",
                "Markdown lesson content",
                existingCourse.getReferenceId());
        var existingLesson2 = new Lesson("Lesson For course2",
                "Description for Lesson2",
                "Markdown lesson content2",
                existingCourse.getReferenceId());
        var existingLesson3 = new Lesson("Lesson For course3",
                "Description for Lesson3",
                "Markdown lesson content3",
                existingCourse.getReferenceId());

        existingCourse = mongoTemplate.save(existingCourse, "course");
        mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "lesson")
                .insert(List.of(existingLesson,existingLesson2,existingLesson3)).execute();

        var result = mockMvc.perform(
                get("/api/learn/lesson")
                        .param("course_id", existingCourse.getReferenceId())
                        .with(user(userDetails))
        ).andExpect(status().isOk()).andReturn().getResponse();
        var jsonResult = jsonPageResponseForLessons.parseObject(result.getContentAsString(StandardCharsets.UTF_8));

        assertThat(jsonResult.getData().content().isEmpty()).isFalse();
        assertThat(jsonResult.getData().content().size()).isEqualTo(3);

        var deleteResultForLesson = mongoTemplate.remove(Query.query(Criteria.where("courseId").is(existingCourse.getReferenceId())),"lesson");
        var deleteResult = mongoTemplate.remove(existingCourse, "course");
        assertThat(deleteResult.wasAcknowledged()).isTrue();
        assertThat(deleteResultForLesson.wasAcknowledged()).isTrue();

    }

    /* =========================================== Test for getting lesson by lessonId ============================== */
    @Autowired
    private JacksonTester<ResponseDetails<LessonResponseFull>> jsonLessonFullResponse;
    @Test
    void shouldGetLessonByTheLessonId() throws Exception {
        var existingLesson = new Lesson("Lesson For course",
                "Description for Lesson",
                "Markdown lesson content",
                UUID.randomUUID().toString());
        existingLesson.setReadIds(Set.of(UUID.randomUUID().toString(),UUID.randomUUID().toString()));
        existingLesson = mongoTemplate.save(existingLesson, "lesson");

        var result = mockMvc.perform(
                get("/api/learn/lesson/%s".formatted(existingLesson.getReferenceId()))
                        .with(user(userDetails))
        ).andExpect(status().isOk()).andReturn().getResponse();

        var responseData = jsonLessonFullResponse
                .parseObject(result.getContentAsString(StandardCharsets.UTF_8)).getData();

        assertThat(existingLesson.getContent()).isEqualTo(responseData.content());
        assertThat(existingLesson.getDescription()).isEqualTo(responseData.description());
        assertThat(existingLesson.getTitle()).isEqualTo(responseData.title());
        assertThat(existingLesson.getReferenceId()).isEqualTo(responseData.lessonId());

        var deleteResult = mongoTemplate.remove(existingLesson, "lesson");
        assertThat(deleteResult.wasAcknowledged()).isTrue();

    }

    @Test
    void shouldReturnNotFound_IfLessonDoesNotExist() throws Exception {
        mockMvc.perform(
                get("/api/learn/lesson/%s".formatted(UUID.randomUUID()))
                        .with(user(userDetails))
        ).andExpect(status().isNotFound());
    }

    /* ============================================ Test for uploading lesson ======================================= */
    @Autowired
    private JacksonTester<LessonUploadRequest> jsonLessonUploadRequest;
    @Autowired
    private JacksonTester<ResponseDetails<LessonResponse>> jsonLessonUploadResponse;

    @Test
    void shouldUploadLesson_GivenAValidRequest() throws Exception{
        var existingCourse = new Course("Course Existing",
                userDetails.getUserReferenceId(),
                "Course Description",
                23000);
        existingCourse.addEnrolledUsers(UUID.randomUUID().toString());

        existingCourse = mongoTemplate.save(existingCourse, "course");

        var lessonUploadRequest = new LessonUploadRequest(existingCourse.getReferenceId(),
                "Lesson title",
                "lesson description",
                "Lesson Content in markdown");

        var result = mockMvc.perform(
                post("/api/learn/lesson")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLessonUploadRequest.write(lessonUploadRequest).getJson())
        )
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        var response = jsonLessonUploadResponse
                .parseObject(result.getContentAsString(StandardCharsets.UTF_8)).getData();

        assertThat(response.title()).isEqualTo(lessonUploadRequest.title());


        var deleteResult = mongoTemplate.remove(existingCourse, "course");
        assertThat(deleteResult.wasAcknowledged()).isTrue();

    }

    @Test
    void shouldReturn400ForAnInValidRequest() throws Exception{
        var existingCourse = new Course("Course Existing",
                userDetails.getUserReferenceId(),
                "Course Description",
                23000);
        existingCourse.addEnrolledUsers(UUID.randomUUID().toString());

        existingCourse = mongoTemplate.save(existingCourse, "course");

        var lessonUploadRequest = new LessonUploadRequest(existingCourse.getReferenceId(),
                "",
                "lesson description",
                "Lesson Content in markdown");

        var result = mockMvc.perform(
                        post("/api/learn/lesson")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonLessonUploadRequest.write(lessonUploadRequest).getJson())
                )
                .andExpect(status().isBadRequest());

        var deleteResult = mongoTemplate.remove(existingCourse, "course");
        assertThat(deleteResult.wasAcknowledged()).isTrue();

    }

    @Test
    void shouldReturn404ForAnNonExistingCourseId() throws Exception{

        var lessonUploadRequest = new LessonUploadRequest(UUID.randomUUID().toString(),
                "Lesson Title",
                "lesson description",
                "Lesson Content in markdown");

        mockMvc.perform(
                        post("/api/learn/lesson")
                                .with(user(userDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonLessonUploadRequest.write(lessonUploadRequest).getJson())
                )
                .andExpect(status().isNotFound());

    }

    /* ============================================== Test for updating lesson ====================================== */

    @Autowired
    private JacksonTester<LessonUpdateRequest> jsonLessonUpdateRequest;
    @Autowired
    private JacksonTester<ResponseDetails<LessonResponse>> jsonLessonUpdateResponse;

    @Test
    void givenExistingCourseAndLesson_ShouldUpdateLesson () throws Exception {
        var existingCourse = new Course("Course Existing",
                userDetails.getUserReferenceId(),
                "Course Description",
                23000);
        existingCourse.addEnrolledUsers(UUID.randomUUID().toString());
        var existingLesson = new Lesson("Lesson For course",
                "Description for Lesson",
                "Markdown lesson content",
                existingCourse.getReferenceId());

        existingCourse = mongoTemplate.save(existingCourse, "course");
        existingLesson = mongoTemplate.save(existingLesson, "lesson");

        var lessonUpdateRequest = new LessonUpdateRequest("updated lesson title",
                "updated lesson description",
                "updated lesson content");

        var result = mockMvc.perform(
                put("/api/learn/lesson/%s".formatted(existingLesson.getReferenceId()))
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLessonUpdateRequest.write(lessonUpdateRequest).getJson())
        ).andExpect(status().isOk()).andReturn().getResponse();

        var response = jsonLessonUpdateResponse
                .parseObject(result.getContentAsString(StandardCharsets.UTF_8)).getData();

        assertThat(existingLesson.getTitle()).isNotEqualTo(response.title());
        assertThat(existingLesson.getDescription()).isNotEqualTo(response.description());
        assertThat(existingLesson.getReferenceId()).isEqualTo(response.lessonId());


        var deleteCourseResult = mongoTemplate.remove(existingCourse, "course");
        assertThat(deleteCourseResult.wasAcknowledged()).isTrue();

        var deleteLessonResult = mongoTemplate.remove(existingLesson, "lesson");
        assertThat(deleteLessonResult.wasAcknowledged()).isTrue();


    }

    @Test
    void givenExistingCourseAndLessonButWithEmptyOrBlankRequestFields_ShouldNotUpdateThoseEmptyFields ()
            throws Exception {
        var existingCourse = new Course("Course Existing",
                userDetails.getUserReferenceId(),
                "Course Description",
                23000);
        existingCourse.addEnrolledUsers(UUID.randomUUID().toString());
        var existingLesson = new Lesson("Lesson For course",
                "Description for Lesson",
                "Markdown lesson content",
                existingCourse.getReferenceId());

        existingCourse = mongoTemplate.save(existingCourse, "course");
        existingLesson = mongoTemplate.save(existingLesson, "lesson");

        var lessonUpdateRequest = new LessonUpdateRequest("",
                "         ",
                "lesson content");

        var result = mockMvc.perform(
                put("/api/learn/lesson/%s".formatted(existingLesson.getReferenceId()))
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLessonUpdateRequest.write(lessonUpdateRequest).getJson())
        ).andExpect(status().isOk()).andReturn().getResponse();

        var response = jsonLessonUpdateResponse
                .parseObject(result.getContentAsString(StandardCharsets.UTF_8)).getData();

        assertThat(existingLesson.getTitle()).isEqualTo(response.title());
        assertThat(existingLesson.getDescription()).isEqualTo(response.description());
        assertThat(existingLesson.getReferenceId()).isEqualTo(response.lessonId());


        var deleteCourseResult = mongoTemplate.remove(existingCourse, "course");
        assertThat(deleteCourseResult.wasAcknowledged()).isTrue();
        var deleteLessonResult = mongoTemplate.remove(existingLesson, "lesson");
        assertThat(deleteLessonResult.wasAcknowledged()).isTrue();


    }

    /* ============================== Test for uploading video for a particular lesson ============================== */

    @Autowired
    private JacksonTester<VideoUploadRequest> jsonVideoUploadRequest;

    @Test
    void shouldUploadVideoForAParticularLesson() throws Exception{
        var existingCourse = new Course("Course Existing",
                userDetails.getUserReferenceId(),
                "Course Description",
                23000);
        existingCourse.addEnrolledUsers(UUID.randomUUID().toString());
        var existingLesson = new Lesson("Lesson For course",
                "Description for Lesson",
                "Markdown lesson content",
                existingCourse.getReferenceId());

        existingCourse = mongoTemplate.save(existingCourse, "course");
        existingLesson = mongoTemplate.save(existingLesson, "lesson");

        var videoUploadRequest = new VideoUploadRequest(
                UUID.randomUUID().toString(),
                existingLesson.getReferenceId(),
                "The video description",
                "The video title",
                Set.of()
        );


        MockMultipartFile metadata = new MockMultipartFile("metadata",
                "metadata",
                MediaType.APPLICATION_JSON_VALUE,
                jsonVideoUploadRequest.write(videoUploadRequest).getJson().getBytes(StandardCharsets.UTF_8)
                );
        MockMultipartFile video = new MockMultipartFile("video",
                "test.mp4",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                new byte[] {});

        mockMvc.perform(
                multipart(HttpMethod.POST,"/api/learn/lesson/video")
                        .file(video)
                        .file(metadata)
                        .with(user(userDetails))

        ).andExpect(status().isCreated());


        var deleteCourseResult = mongoTemplate.remove(existingCourse, "course");
        assertThat(deleteCourseResult.wasAcknowledged()).isTrue();
        var deleteLessonResult =  mongoTemplate.remove(existingLesson, "lesson");
        assertThat(deleteLessonResult.wasAcknowledged()).isTrue();

    }

}
