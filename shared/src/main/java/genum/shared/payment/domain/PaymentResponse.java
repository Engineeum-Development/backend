package genum.shared.payment.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import genum.shared.payment.constants.PaymentStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(String time, PaymentStatus status, PaymentResponseData data) {
}
