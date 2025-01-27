package genum.shared.payment.DTO;

import genum.shared.payment.constants.PaymentStatus;

public record PaymentDTO(String userId, PaymentStatus paymentStatus, int paymentValue) {

}
