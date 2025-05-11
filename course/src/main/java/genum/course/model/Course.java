package genum.course.model;

import genum.shared.course.DTO.CourseDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Document
@Getter
@Setter
public class Course {
    @MongoId
    private String id;
    @Indexed(unique = true)
    private String referenceId;
    private String name;
    private Set<String> enrolledUsers;
    private String uploaderId;
    private String description;
    private int price;
    private LocalDateTime uploadDate;

    public Course(String name,String uploaderId, String description, int price) {
        this.referenceId = UUID.randomUUID().toString();
        this.name = name;
        this.enrolledUsers = new TreeSet<>();
        this.uploaderId = uploaderId;
        this.description = description;
        this.price = price;
        this.uploadDate = LocalDateTime.now();
    }

    public CourseDTO toDTO() {
        return new CourseDTO(this.referenceId, this.name, this.uploaderId, this.enrolledUsers.size(), this.price, this.description, this.uploadDate.toString());
    }

    public void addEnrolledUsers(String userId) {
        this.enrolledUsers.add(userId);
    }


}
