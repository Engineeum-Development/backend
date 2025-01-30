package genum.genumUser.repository;

import genum.genumUser.model.OneTimeToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OneTimeTokenRepository extends MongoRepository<OneTimeToken, String> {

    Optional<OneTimeToken> findOneTimeTokenByToken(String token);
}
