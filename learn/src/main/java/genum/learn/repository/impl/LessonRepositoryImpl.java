package genum.learn.repository.impl;

import genum.learn.dto.LessonDTO;
import genum.learn.model.Lesson;
import genum.learn.repository.LessonRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LessonRepositoryImpl implements LessonRepositoryCustom {

    private final MongoTemplate mongoTemplate;
    @Override
    @Cacheable(value = "paged_lesson_by_courseId", key = "@customPageableKeyGenerator")
    public Page<LessonDTO> findAllByCourseId(String courseId, Pageable pageable) {
        var size = pageable.getPageSize();
        var offset = pageable.getOffset();
        MatchOperation matchOperation = Aggregation.match(Criteria.where("courseId").is(courseId));
        ProjectionOperation projectionOperation = Aggregation.project()
                .and("referenceId").as("lessonId")
                .and("courseId").as("courseId")
                .and("title").as("title")
                .and("description").as("description")
                .and("content").as("content")
                .and("readIds").size().as("reads");
        SkipOperation skipOperation = Aggregation.skip(offset);
        LimitOperation limitOperation = Aggregation.limit(size);

        TypedAggregation<Lesson> aggregation = Aggregation.newAggregation(Lesson.class, matchOperation,projectionOperation, skipOperation, limitOperation);

        AggregationResults<LessonDTO> results = mongoTemplate.aggregate(aggregation, LessonDTO.class);

        return new PageImpl<>(results.getMappedResults(),pageable,results.getMappedResults().size());
    }
    @Override
    @Cacheable(value = "lesson_by_id", key = "#lessonId")
    public Optional<LessonDTO> findDTOByReferenceId(String lessonId){
        MatchOperation matchOperation = Aggregation.match(Criteria.where("referenceId").is(lessonId));
        ProjectionOperation projectionOperation = Aggregation.project("title","description","content","courseId")
                .and("referenceId").as("lessonId")
                .and("readIds").size().as("reads");
        TypedAggregation<Lesson> aggregation = Aggregation.newAggregation(Lesson.class, matchOperation,projectionOperation);

        AggregationResults<LessonDTO> results = mongoTemplate.aggregate(aggregation, LessonDTO.class);

        return Optional.ofNullable(results.getUniqueMappedResult());
    }
}
