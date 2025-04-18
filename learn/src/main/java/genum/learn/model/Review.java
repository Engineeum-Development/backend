package genum.learn.model;

import genum.learn.dto.ReviewDTO;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
@Getter
public class Review {
    private final String comment;
    private final int rating;
    private final String userId;
    private final String courseId;
    @MongoId
    private String id;

    public Review(String comment, int rating, String userId, String courseId) {
        this.comment = comment;
        this.rating = rating;
        this.userId = userId;
        this.courseId = courseId;
    }

    public ReviewDTO toDTO() {
        return new ReviewDTO(this.courseId,this.rating,this.comment);
    }
}
