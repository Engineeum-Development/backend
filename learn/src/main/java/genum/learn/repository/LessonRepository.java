package genum.learn.repository;

import genum.learn.model.Lesson;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonRepository extends MongoRepository<Lesson, String> {

    Optional<Lesson> findByReferenceId(String lessonId);

    void deleteByReferenceId(String lessonId);


}
