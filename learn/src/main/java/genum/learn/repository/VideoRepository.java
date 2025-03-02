package genum.learn.repository;

import genum.learn.model.Video;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoRepository extends MongoRepository<Video, String> {

    Optional<Video> findByVideoId(String videoId);
}
