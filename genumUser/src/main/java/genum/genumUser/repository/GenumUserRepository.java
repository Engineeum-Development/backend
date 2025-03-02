package genum.genumUser.repository;

import genum.genumUser.model.GenumUser;
import genum.shared.genumUser.GenumUserDTO;
import genum.shared.security.CustomUserDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GenumUserRepository extends MongoRepository<GenumUser, String> {

    boolean existsByCustomUserDetailsEmail(String email);

    GenumUser findByCustomUserDetailsEmail(String email);
    GenumUser findByCustomUserDetails_UserReferenceId(String referenceId);

    @Query(value = " {customUserDetails.email: ?0}", fields = "{_id: 1}")
    String findUserIdByEmail(String email);

}
