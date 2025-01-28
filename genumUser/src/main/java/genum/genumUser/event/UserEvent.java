package genum.genumUser.event;

import genum.genumUser.model.GenumUser;
import genum.shared.events.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;

public record UserEvent(GenumUser user, UserEventType userEventType, Map<String,String> data) implements DomainEvent {

    private static LocalDateTime localDateTime;

    public UserEvent {
        localDateTime = LocalDateTime.now();
    }

    @Override
    public LocalDateTime getTimeStamp() {
        return localDateTime;
    }
}
