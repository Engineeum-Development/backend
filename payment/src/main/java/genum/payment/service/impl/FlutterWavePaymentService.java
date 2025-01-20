package genum.payment.service.impl;

import genum.payment.domain.PaymentResponse;
import genum.payment.domain.ProductRequest;
import genum.payment.service.PaymentService;
import org.springframework.stereotype.Service;

@Service
public class FlutterWavePaymentService implements PaymentService {
    @Override
    public PaymentResponse initializePayment(ProductRequest productRequest) {
        return null;
    }

    @Override
    public PaymentResponse verifyPayment(String reference, String paymentId) {
        return null;
    }
}
