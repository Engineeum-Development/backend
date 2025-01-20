package genum.data.serviceimplementation.payment;

import genum.data.shared.payment.domain.PaymentResponse;
import genum.data.shared.payment.domain.ProductRequest;
import genum.data.payment.service.PaymentService;
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
