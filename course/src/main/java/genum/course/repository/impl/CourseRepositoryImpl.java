package genum.course.repository.impl;

import genum.course.model.Course;
import genum.course.repository.CourseRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CourseRepositoryImpl implements CourseRepositoryCustom {

    private final MongoTemplate mongoTemplate;
    @Override
    public boolean existsByReferenceIdAndEnrolledUsersContaining(String courseId, String userId) {
        if (Objects.nonNull(courseId) && Objects.nonNull(userId)) {
            Query query = Query.query(Criteria.where("referenceId").is(courseId));
            var course = mongoTemplate.findOne(query, Course.class, "course");

            if (course == null) return false;

            return course.getEnrolledUsers().contains(userId);
        }
        return false;
    }
}
