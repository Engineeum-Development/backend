package genum.shared.payment.DTO;

import genum.shared.payment.constants.PaymentStatus;

import java.io.Serializable;

public record PaymentDTO(String userId,String courseId,String transactionRef, PaymentStatus paymentStatus, int paymentValue) implements Serializable {

}
