package genum.learn.repository;

import genum.learn.model.VideoUploadStatusModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VideoUploadStatusRepository extends MongoRepository<VideoUploadStatusModel, String> {

    Optional<VideoUploadStatusModel> getVideoUploadStatusModelByVideoId(String videoId);
}
