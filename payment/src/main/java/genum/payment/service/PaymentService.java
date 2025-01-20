package genum.payment.service;

import genum.payment.domain.PaymentResponse;
import genum.payment.domain.ProductRequest;
import org.springframework.stereotype.Service;


public interface PaymentService {
    PaymentResponse initializePayment(ProductRequest productRequest);

    PaymentResponse verifyPayment(String reference, String paymentId);
}
