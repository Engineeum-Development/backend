package genum.learn.dto;

import genum.learn.model.Review;

import java.time.LocalDateTime;
import java.util.Set;

public record CourseDetailedResponse(String name,
                                     String description,
                                     String uploader,
                                     String numberOfEnrolledUsers,
                                     String rating,
                                     String uploadDate,
                                     Set<ReviewData> reviews) {
}
