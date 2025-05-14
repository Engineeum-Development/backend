package genum.email.repository;

import genum.email.constant.EmailStatus;
import genum.email.model.Email;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailRepository extends MongoRepository<Email, String> {

    List<Email> findTop50ByStatus(EmailStatus emailStatus);
}
