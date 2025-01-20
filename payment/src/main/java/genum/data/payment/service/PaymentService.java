package genum.data.payment.service;

import genum.data.shared.payment.domain.PaymentResponse;
import genum.data.shared.payment.domain.ProductRequest;


public interface PaymentService {
    PaymentResponse initializePayment(ProductRequest productRequest);

    PaymentResponse verifyPayment(String reference, String paymentId);
}
