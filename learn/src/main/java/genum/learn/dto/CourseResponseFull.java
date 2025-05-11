package genum.learn.dto;

import java.util.Set;

public record CourseResponseFull(String name,
                                 String description,
                                 String uploader,
                                 String numberOfEnrolledUsers,
                                 String rating,
                                 String uploadDate,
                                 Set<ReviewData> reviews) {
}
