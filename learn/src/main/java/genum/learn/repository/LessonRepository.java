package genum.learn.repository;

import genum.learn.model.Lesson;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonRepository extends MongoRepository<Lesson, String> {

    @Cacheable(value = "lesson_by_id", key = "#lessonId")
    Optional<Lesson> findByReferenceId(String lessonId);
    boolean existsByReferenceId(String lessonId);

    void deleteByReferenceId(String lessonId);


}
