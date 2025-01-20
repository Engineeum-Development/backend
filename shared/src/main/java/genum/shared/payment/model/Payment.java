package genum.shared.payment.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "payment")
@Getter
@Setter
public class Payment {
    @MongoId
    private String id;
    @Field("user_id")
    private String userid;
    @Field("product_id")
    private String productId;
}
