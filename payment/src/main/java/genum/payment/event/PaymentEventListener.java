package genum.payment.event;

import genum.payment.service.PaymentService;
import genum.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final ProductService productService;

    @EventListener
    public void onPaymentEvent(PaymentEvent paymentEvent) {
        switch (paymentEvent.getEventType()) {
            case PAYMENT_FAILED -> {}
            case PAYMENT_SUCCESSFUL -> {
                productService.enrollCurrentUser(paymentEvent.getPayment().userId());
            }
        }
    }


}
