package genum.persistence.genumUserRepository;

import genum.data.genumUser.GenumUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenumUserRepository extends MongoRepository<GenumUser, String> {
    Boolean existsByEmail(String email);
    GenumUser findByEmail(String email);
}
