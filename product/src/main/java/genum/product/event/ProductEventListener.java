package genum.product.event;

import genum.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductService productService;

    @EventListener
    public void onProductEvent(ProductEvent domainEvent) {
        switch (domainEvent.getEventType()) {
            case ENROLLED -> {
                productService.incrementCourseEnrolled(domainEvent.getCourse().referenceId());
            }
            case PRICE_CHANGE -> {}
        }

    }
}
