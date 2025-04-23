package genum.shared.payment.DTO;

import genum.shared.payment.constants.PaymentStatus;

import java.io.Serializable;

public record PaymentDTO(String userId, PaymentStatus paymentStatus, int paymentValue) implements Serializable {

}
