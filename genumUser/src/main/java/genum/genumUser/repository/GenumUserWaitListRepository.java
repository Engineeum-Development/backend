package genum.genumUser.repository;

import genum.genumUser.model.WaitListEmail;
import genum.shared.genumUser.WaitListEmailDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface GenumUserWaitListRepository extends MongoRepository<WaitListEmail, String> {

    boolean existsByEmail(String email);

    @Query(value = "{}", fields = "{ email:  1}")
    Page<WaitListEmailDTO> findPagedWaitingList(Pageable pageable);
}
