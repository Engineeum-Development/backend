package genum.genumUser.repository;

import genum.genumUser.model.OneTimeToken;
import genum.genumUser.repository.projection.IdOnly;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OneTimeTokenRepository extends MongoRepository<OneTimeToken, String> {

    Optional<OneTimeToken> findOneTimeTokenByToken(String token);
    List<IdOnly> findTop50ByExpiryBeforeOrderByExpiryDesc(LocalDateTime dateTime);

}
