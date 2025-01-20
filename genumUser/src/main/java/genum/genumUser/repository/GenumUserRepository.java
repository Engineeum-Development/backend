package genum.genumUser.repository;

import genum.shared.data.genumUser.GenumUser;
import genum.shared.security.CustomUserDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GenumUserRepository extends MongoRepository<GenumUser, String> {
    Boolean existsByEmail(String email);
    GenumUser findByEmail(String email);
    @Query(value = """
            {
                'email': { $eq: ?0 }
            }
                """,
            fields = """
            {
                'email': 1, 'password': 1
            }
            """)
    CustomUserDetails getCustomUserDetailsByEmail(String email);


}
