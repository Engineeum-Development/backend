package genum.product.service;

import genum.product.model.Course;
import genum.shared.product.DTO.CourseDTO;
import genum.shared.product.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import genum.product.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    public CourseDTO findCourseById(String id) {
        Course course = productRepository.findById(id).orElseThrow(ProductNotFoundException::new);
        return new CourseDTO(course.getReferenceId(), course.getName(), course.getNumberOfEnrolledUsers(), course.getPrice());
    }
    public CourseDTO findCourseByReference(String referenceId) {
        Course course = productRepository.findByReferenceId(referenceId).orElseThrow(ProductNotFoundException::new);
        return new CourseDTO(course.getReferenceId(), course.getName(), course.getNumberOfEnrolledUsers(), course.getPrice());
    }
}
