package genum.product.model;

import genum.shared.product.DTO.CourseDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
@Getter
@Setter
public class Course {
    @MongoId
    private  String id;
    @Indexed(unique = true)
    private String referenceId;
    private String name;
    @Field("number_of_enrolled_users")
    private int numberOfEnrolledUsers;
    private int price;

    public CourseDTO toDTO() {
        return new CourseDTO(this.referenceId, this.name, this.numberOfEnrolledUsers, this.price);
    }


}
