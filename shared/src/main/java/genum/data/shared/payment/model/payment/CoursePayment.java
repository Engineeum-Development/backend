package genum.data.shared.payment.model.payment;

import genum.data.shared.payment.constants.PaymentStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

@Document(collection = "payment")
@Getter
@Setter
public class CoursePayment {
    @MongoId
    private String id;
    @Field("user_id")
    private String userid;
    @Field("payment_status")
    private PaymentStatus paymentStatus;
    @Field("course_id")
    private String courseId;
    @Field("payment_value")
    private int paymentValue;
    private LocalDateTime paymentInitializationDate;
}
