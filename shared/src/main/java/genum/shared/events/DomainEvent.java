package genum.shared.events;

import java.time.LocalDateTime;

public interface DomainEvent {
    default LocalDateTime getTimeStamp() {
        return LocalDateTime.now();
    }
}
