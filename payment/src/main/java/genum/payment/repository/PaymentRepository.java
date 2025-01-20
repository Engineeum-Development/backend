package genum.payment.repository;

import genum.shared.payment.model.payment.CoursePayment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends MongoRepository<CoursePayment, String> {
}
