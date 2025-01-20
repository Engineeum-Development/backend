package genum.serviceimplementation.payment;

import genum.shared.payment.domain.PaymentResponse;
import genum.shared.payment.domain.ProductRequest;
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
