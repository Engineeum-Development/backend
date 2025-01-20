package genum.payment.repository;

import genum.shared.payment.model.product.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Course, String> {
}
