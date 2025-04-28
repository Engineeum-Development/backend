package genum.payment.model;

import genum.payment.constant.PaymentPlatform;
import genum.shared.payment.DTO.PaymentDTO;
import genum.shared.payment.constants.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

@Document(collection = "payment")
@Getter
@Setter
@Builder
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
    @Field("txRef")
    private String transactionReference;
    private PaymentPlatform paymentPlatForm;
    private String paymentInitializationDate;

    public PaymentDTO toPaymentDTO() {
        return new PaymentDTO(this.userid,this.courseId,this.transactionReference, this.paymentStatus, this.paymentValue);
    }
}
