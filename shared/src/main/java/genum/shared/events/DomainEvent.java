package genum.shared.events;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime getTimeStamp();
}
