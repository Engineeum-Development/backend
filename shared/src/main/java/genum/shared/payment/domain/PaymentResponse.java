package genum.shared.payment.domain;

import genum.shared.payment.constants.PaymentStatus;

import java.time.LocalDateTime;
import java.util.Map;

public record PaymentResponse(LocalDateTime time, PaymentStatus status, Map<String,String> info) {
}
