package genum.payment.event;

import genum.shared.events.DomainEvent;
import genum.shared.payment.DTO.PaymentDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
public class PaymentEvent implements DomainEvent {
    private PaymentDTO payment;
    private EventType eventType;
    private LocalDateTime time;
    private Map<String, ?> data;

    @Override
    public LocalDateTime getTimeStamp() {
        return time;
    }
}
