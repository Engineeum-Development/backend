package genum.shared.payment.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class PaymentResponseData {

    private String message;
    private String authorizationUrl;
    private String accessCode;
    private String reference;

}
