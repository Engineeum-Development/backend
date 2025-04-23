package genum.genumUser.repository;

import genum.genumUser.model.GenumUser;
import genum.genumUser.repository.projection.GenumUserWithIDFirstNameLastName;
import genum.shared.genumUser.GenumUserDTO;
import genum.shared.security.CustomUserDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenumUserRepository extends MongoRepository<GenumUser, String> {

    boolean existsByCustomUserDetailsEmail(String email);

    Optional<GenumUser> findByCustomUserDetailsEmail(String email);
    Optional<GenumUser> findByCustomUserDetails_UserReferenceId(String referenceId);
    @Query(value = "{'customUserDetails.userReferenceId': ?0}",
            fields = "{'customUserDetails.userReferenceId': 1,'firstName': 1,'lastName': 1}")
    List<GenumUserWithIDFirstNameLastName> findByCustomUserDetails_UserReferenceIdReturningIdFirstAndName(String referenceId);

    @Query(value = " {customUserDetails.email: ?0}", fields = "{_id: 1}")
    String findUserIdByEmail(String email);

}
