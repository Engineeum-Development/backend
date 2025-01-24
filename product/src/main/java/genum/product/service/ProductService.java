package genum.product.service;

import genum.product.model.Course;
import genum.shared.product.DTO.CourseDTO;
import genum.shared.product.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import genum.product.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    @Transactional(readOnly = true)
    public CourseDTO findCourseById(String id) {
        Course course = productRepository.findById(id).orElseThrow(ProductNotFoundException::new);
        return new CourseDTO(course.getReferenceId(), course.getName(), course.getNumberOfEnrolledUsers(), course.getPrice());
    }
    @Transactional(readOnly = true)
    public CourseDTO findCourseByReference(String referenceId) {
        Course course = productRepository.findByReferenceId(referenceId).orElseThrow(ProductNotFoundException::new);
        return new CourseDTO(course.getReferenceId(), course.getName(), course.getNumberOfEnrolledUsers(), course.getPrice());
    }

    @Transactional
    public void incrementCourseEnrolled(String courseReferenceId) {
        Course course = productRepository.findByReferenceId(courseReferenceId).orElseThrow(ProductNotFoundException::new);
        var currentNumberOfEnrolledUsers = course.getNumberOfEnrolledUsers();
        course.setNumberOfEnrolledUsers(++currentNumberOfEnrolledUsers);
        productRepository.save(course);
    }
    @Transactional
    public CourseDTO updateProductPrice(String courseReferenceId, int newPrice) {
        var course = productRepository.findByReferenceId(courseReferenceId).orElseThrow(ProductNotFoundException::new);
        course.setPrice(newPrice);
        course = productRepository.save(course);
        return course.toDTO();
    }

}
