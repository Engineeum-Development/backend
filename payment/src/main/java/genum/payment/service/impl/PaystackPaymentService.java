package genum.payment.service.impl;

import genum.shared.payment.domain.PaymentResponse;
import genum.shared.payment.domain.ProductRequest;
import genum.payment.service.PaymentService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public final class PaystackPaymentService implements PaymentService {
    @Override
    public PaymentResponse initializePayment(ProductRequest productRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String paymentId = "";
        if (authentication == null || !authentication.isAuthenticated()) {
            throw
        }

        return null;
    }

    @Override
    public PaymentResponse verifyPayment(String reference, String paymentId) {
        return null;
    }
}
