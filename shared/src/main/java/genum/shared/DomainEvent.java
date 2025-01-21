package genum.shared;

import java.time.LocalDateTime;

public interface DomainEvent {
    String getEventType();
    LocalDateTime getTimeStamp();
}
