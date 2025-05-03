package genum.payment.repository;


import genum.payment.model.CoursePayment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<CoursePayment, String> {

    Optional<CoursePayment> findByTransactionReference(String transactionRef);
}
