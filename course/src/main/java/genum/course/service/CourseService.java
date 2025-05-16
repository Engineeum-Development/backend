package genum.course.service;

import genum.course.repository.CourseRepository;
import genum.course.model.Course;
import genum.shared.course.DTO.CourseDTO;
import genum.shared.security.SecurityUtils;
import genum.shared.course.exception.CourseNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final SecurityUtils securityUtils;


    public Page<CourseDTO> findCourseWithUploaderId(String id, Pageable pageable) {
        return courseRepository.findAllByUploaderId(id, pageable).map(Course::toDTO);
    }
    @Transactional(readOnly = true)
    @Cacheable(value = "course_by_id", key = "#referenceId")
    public CourseDTO findCourseByReference(String referenceId) {
        Course course = courseRepository.findByReferenceId(referenceId).orElseThrow(CourseNotFoundException::new);
        return course.toDTO();
    }

    @CachePut(value = "course_by_id", key = "#courseDTO.referenceId()", condition = "{T(java.util.Objects).nonNull(#courseDTO.referenceId())}")
    public CourseDTO createCourse(CourseDTO courseDTO) {
        Course course = new Course(courseDTO.name(),courseDTO.uploader(), courseDTO.description(), courseDTO.price());
        return courseRepository.save(course).toDTO();
    }
    public Page<CourseDTO> findAllCourses(Pageable pageable) {
        var courses = courseRepository.findAll(pageable);
        return courses.map(Course::toDTO);
    }
    @Cacheable(value = "current_user_enrolled", key = "#courseId")
    public boolean userIdHasEnrolledForCourse(String courseId) {
        var userId = securityUtils.getCurrentAuthenticatedUserId();
        return courseRepository.existsByReferenceIdAndEnrolledUsersContaining(courseId, userId);
    }

    @CacheEvict(value = "user_enrolled",key = "(@securityUtils.getCurrentAuthenticatedUserId())+''+#courseReferenceId")
    public void enrollCurrentUser(String courseReferenceId) {
        var currentUserId = securityUtils.getCurrentAuthenticatedUserId();
        var course = courseRepository.findByReferenceId(courseReferenceId).orElseThrow(CourseNotFoundException::new);
        course.addEnrolledUsers(currentUserId);
        courseRepository.save(course);
    }

    @CachePut(value = "course_by_id", key = "#courseReferenceId")
    public CourseDTO updateProductPrice(String courseReferenceId, int newPrice) {
        var course = courseRepository.findByReferenceId(courseReferenceId).orElseThrow(CourseNotFoundException::new);
        course.setPrice(newPrice);
        course = courseRepository.save(course);
        return course.toDTO();
    }



}
