package genum.course.repository;

public interface CourseRepositoryCustom {

    boolean existsByReferenceIdAndEnrolledUsersContaining(String courseId, String userId);
}
