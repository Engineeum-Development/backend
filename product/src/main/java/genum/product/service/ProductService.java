package genum.product.service;

import genum.product.model.Course;
import genum.product.repository.ProductRepository;
import genum.shared.product.DTO.CourseDTO;
import genum.shared.product.exception.ProductNotFoundException;
import genum.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final SecurityUtils securityUtils;
    @Transactional(readOnly = true)
    public CourseDTO findCourseById(String id) {
        Course course = productRepository.findById(id).orElseThrow(ProductNotFoundException::new);
        return course.toDTO();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "courses_by_id_page", keyGenerator = "customPageableKeyGenerator")
    public Page<CourseDTO> findCourseWithUploaderId(String id, Pageable pageable) {
        return productRepository.findAllByUploaderId(id, pageable).map(Course::toDTO);
    }
    @Transactional(readOnly = true)
    @Cacheable(value = "course_by_id", key = "#referenceId")
    public CourseDTO findCourseByReference(String referenceId) {
        Course course = productRepository.findByReferenceId(referenceId).orElseThrow(ProductNotFoundException::new);
        return course.toDTO();
    }

    @CachePut(value = "course_by_id", key = "#courseDTO.referenceId()")
    public CourseDTO createCourse(CourseDTO courseDTO) {
        Course course = new Course(courseDTO.name(),courseDTO.uploader(), courseDTO.description(), courseDTO.price());
        return productRepository.save(course).toDTO();
    }
    @Cacheable(value = "course_all_page", keyGenerator = "customPageableKeyGenerator")
    public Page<CourseDTO> findAllCourses(Pageable pageable) {
        var courses = productRepository.findAll(pageable);
        return courses.map(Course::toDTO);
    }
    @Cacheable(value = "user_enrolled", key = "#courseId+userId")
    public boolean userIdHasEnrolledForCourse(String courseId, String userId) {
        return productRepository.existsByReferenceIdAndEnrolledUsersContaining(courseId, userId);
    }

    @CachePut(value = "user_enrolled", key = "#courseReferenceId+#securityUtils.currentAuthenticatedUserId")
    public void enrollCurrentUser(String courseReferenceId) {
        var currentUserId = securityUtils.getCurrentAuthenticatedUserId();
        var course = productRepository.findByReferenceId(courseReferenceId).orElseThrow(ProductNotFoundException::new);
        course.addEnrolledUsers(currentUserId);
        productRepository.save(course);
    }

    @CachePut(value = "course_by_id", key = "#courseReferenceId")
    public CourseDTO updateProductPrice(String courseReferenceId, int newPrice) {
        var course = productRepository.findByReferenceId(courseReferenceId).orElseThrow(ProductNotFoundException::new);
        course.setPrice(newPrice);
        course = productRepository.save(course);
        return course.toDTO();
    }



}
