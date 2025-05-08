package genum.payment.service;

import genum.payment.domain.WebHook;
import genum.shared.payment.domain.PaymentResponse;
import genum.shared.payment.domain.ProductRequest;


public interface PaymentService {
    PaymentResponse initializePayment(ProductRequest productRequest);

    PaymentResponse verifyPayment(String reference);

    boolean handleWebHook(WebHook webHook);
}
