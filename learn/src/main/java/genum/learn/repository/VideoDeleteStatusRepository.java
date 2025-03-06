package genum.learn.repository;

import genum.learn.enums.VideoDeleteStatus;
import genum.learn.model.VideoDeleteStatusModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VideoDeleteStatusRepository extends MongoRepository<VideoDeleteStatusModel, String> {
    List<VideoDeleteStatusModel> findAllByVideoDeleteStatus(VideoDeleteStatus status);
}
