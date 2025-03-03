package genum.product.repository;

import genum.product.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Course, String> {

    Optional<Course> findByReferenceId(String referenceId);
    Page<Course> findAllByUploaderId(String uploaderId, Pageable pageable);
    boolean existsByReferenceIdAndEnrolledUsersContaining(String courseId, String userId);
}
