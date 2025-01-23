package genum.payment.repository;


import genum.payment.model.CoursePayment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends MongoRepository<CoursePayment, String> {
}
