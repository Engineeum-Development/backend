package genum.data.payment.repository;

import genum.data.shared.payment.model.payment.CoursePayment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends MongoRepository<CoursePayment, String> {
}
