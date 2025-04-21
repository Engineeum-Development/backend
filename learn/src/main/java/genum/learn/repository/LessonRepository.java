package genum.learn.repository;

import genum.learn.model.Lesson;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonRepository extends MongoRepository<Lesson, String> {

    @Cacheable(value = "lesson_by_id", key = "#lessonId")
    Optional<Lesson> findByReferenceId(String lessonId);
    @Cacheable(value = "lessons_by_course_id")
    Page<Lesson> findAllByCourseId(String courseId, Pageable pageable);
    boolean existsByReferenceId(String lessonId);
    @CacheEvict(value = "lesson_by_id", key = "#lessonId")
    void deleteByReferenceId(String lessonId);


}
