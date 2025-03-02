package genum.learn.repository;

import genum.learn.model.VideoSeries;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoSeriesRepository extends MongoRepository<VideoSeries, String> {
    /*Optional<VideoSeries> findByCourseReference(String courseReferenceID);
    Optional<VideoSeries> findByReference(String seriesReference);*/
}
