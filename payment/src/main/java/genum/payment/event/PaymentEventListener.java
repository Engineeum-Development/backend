package genum.payment.event;

import genum.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final CourseService courseService;

    @EventListener
    public void onPaymentEvent(PaymentEvent paymentEvent) {
        switch (paymentEvent.getEventType()) {
            case PAYMENT_FAILED -> {}
            case PAYMENT_SUCCESSFUL -> courseService.enrollCurrentUser(paymentEvent.getPayment().courseId());
        }
    }


}
