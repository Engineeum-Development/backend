package genum.learn.repository;

import genum.learn.dto.LessonDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LessonRepositoryCustom {


    Page<LessonDTO> findAllByCourseId(String courseId, Pageable pageable);
    Optional<LessonDTO> findDTOByReferenceId(String lessonId);
}
