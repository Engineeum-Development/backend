package genum.learn.model;

import genum.learn.dto.ReviewDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
public class Review {
    private final String comment;
    private final double rating;
    private final String userId;
    private final String courseId;
    @Id
    @Setter
    private String id;

    public Review(String comment, int rating, String userId, String courseId) {
        this.comment = comment;
        this.rating = rating;
        this.userId = userId;
        this.courseId = courseId;
    }

    public ReviewDTO toDTO() {
        return new ReviewDTO(this.userId, this.courseId,(int)this.rating,this.comment);
    }
}
