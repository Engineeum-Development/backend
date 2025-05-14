package genum.learn.repository;

import genum.learn.dto.LessonDTO;
import genum.learn.model.Lesson;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonRepository extends MongoRepository<Lesson, String>, LessonRepositoryCustom {

    boolean existsByReferenceId(String lessonId);
    boolean existsByTitle(String title);
    @CacheEvict(value = "lesson_by_id", key = "#lessonId")
    void deleteByReferenceId(String lessonId);

    Optional<Lesson> findByReferenceId(String lessonId);


}
